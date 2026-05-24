package com.example.threadsasynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView txtStatus;
    private ProgressBar progressBar;
    private ImageView img;
    private Handler mainHandler;  // Pour revenir sur le Thread UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Liaison des vues
        txtStatus = findViewById(R.id.txtStatus);
        progressBar = findViewById(R.id.progressBar);
        img = findViewById(R.id.img);

        Button btnLoadThread = findViewById(R.id.btnLoadThread);
        Button btnCalcAsync = findViewById(R.id.btnCalcAsync);
        Button btnToast = findViewById(R.id.btnToast);

        // Handler attaché au main thread
        mainHandler = new Handler(Looper.getMainLooper());

        // Bouton Toast : toujours réactif
        btnToast.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "UI réactive !", Toast.LENGTH_SHORT).show()
        );

        // Chargement image avec un Thread classique
        btnLoadThread.setOnClickListener(v -> loadImageWithThread());

        // Calcul lourd avec AsyncTask
        btnCalcAsync.setOnClickListener(v -> new HeavyCalcTask().execute());
    }

    // ------------------- PARTIE 1 : THREAD + HANDLER -------------------
    private void loadImageWithThread() {
        // Afficher la progression sur l'UI Thread
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        txtStatus.setText("Statut : chargement image (Thread)...");

        // Création et démarrage du worker thread
        new Thread(() -> {
            // Simuler un téléchargement / travail long
            try {
                Thread.sleep(1000);   // 1 seconde d'attente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Charger l'image (icône par défaut du projet)
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            // Retour sur l'UI Thread pour mettre à jour les vues
            mainHandler.post(() -> {
                img.setImageBitmap(bitmap);
                progressBar.setVisibility(View.INVISIBLE);
                txtStatus.setText("Statut : image chargée (Thread)");
            });
        }).start();
    }

    // ------------------- PARTIE 2 : ASYNCTASK (calcul lourd) -------------------
    private class HeavyCalcTask extends AsyncTask<Void, Integer, Long> {

        // Avant le traitement : UI Thread
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            txtStatus.setText("Statut : calcul lourd (AsyncTask)...");
        }

        // Travail long en arrière-plan : worker thread
        @Override
        protected Long doInBackground(Void... voids) {
            long result = 0;
            for (int i = 1; i <= 100; i++) {
                // Simulation d'un calcul très lourd
                for (int k = 0; k < 2_000_000; k++) {
                    result += (i * k) % 7;
                }
                // Mise à jour de la progression (envoie à onProgressUpdate)
                publishProgress(i);
            }
            return result;
        }

        // Pendant le traitement : UI Thread (reçoit la progression)
        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            progressBar.setProgress(progress);
            // Optionnel : afficher le pourcentage dans le TextView
            txtStatus.setText("Statut : calcul lourd - " + progress + " %");
        }

        // Après le traitement : UI Thread
        @Override
        protected void onPostExecute(Long result) {
            progressBar.setVisibility(View.INVISIBLE);
            txtStatus.setText("Statut : calcul terminé. Résultat = " + result);
        }
    }
}