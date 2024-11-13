package com.example.api7

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.AlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class up_and_del : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_up_and_del)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val up_name = findViewById<EditText>(R.id.up_editTextName)
        val up_prix = findViewById<EditText>(R.id.up_editTextPrice)
        val up_image = findViewById<EditText>(R.id.up_editTextImage)
        val up_switch = findViewById<Switch>(R.id.up_switchHaveFP)
        val up_btn = findViewById<Button>(R.id.buttonUP)
        val del_button = findViewById<Button>(R.id.buttonDEL)

        // Récupérer les données passées via l'intent
        val pcId = intent.getIntExtra("id", 0)
        val nom = intent.getStringExtra("name")
        val prix = intent.getDoubleExtra("prix", 0.0)
        val check = intent.getBooleanExtra("check", false)
        val image = intent.getStringExtra("image")

        // Initialiser les champs
        up_name.setText(nom)
        up_prix.setText(prix.toString())
        up_image.setText(image)
        up_switch.isChecked = check

        // Bouton de mise à jour avec une boîte de dialogue de confirmation
        up_btn.setOnClickListener {
            val updatedName = up_name.text.toString()
            val updatedPrice = up_prix.text.toString().toDoubleOrNull() ?: 0.0
            val updatedImage = up_image.text.toString()
            val haveFP = up_switch.isChecked

            Log.d("SwitchState", "haveFP: $haveFP")

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Confirmer la mise à jour")
                .setMessage("Voulez-vous vraiment mettre à jour cet ordinateur ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://apiyes.net/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val apiService = retrofit.create(ApiService::class.java)

                    // Créer l'objet Ordinateur
                    val up_pc = Ordinateur(pcId, updatedName, updatedPrice, haveFP, updatedImage)
                    Log.d("OrdinateurData", "Sending data: $up_pc")

                    apiService.upPC(up_pc).enqueue(object : Callback<AddResponse> {
                        override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(applicationContext, "Mise à jour réussie", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Log.e("UpdateError", "Error code: ${response.code()}, message: ${response.message()}")
                                val errorBody = response.errorBody()?.string()
                                Log.e("UpdateErrorBody", errorBody ?: "No error body")
                                Toast.makeText(applicationContext, "Échec de la mise à jour: $errorBody", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                            Toast.makeText(applicationContext, "Erreur: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }

        // Bouton de suppression avec une boîte de dialogue de confirmation
        del_button.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer cet ordinateur ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://apiyes.net/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val apiService = retrofit.create(ApiService::class.java)

                    val del_pc = Ordinateur(pcId, "", prix, check, image!!)
                    Log.d("DeleteData", "Sending data: $del_pc")

                    apiService.deletePC(del_pc).enqueue(object : Callback<AddResponse> {
                        override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(applicationContext, "Ordinateur supprimé avec succès", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("DeleteError", "Error code: ${response.code()}, message: ${response.message()}")
                                Toast.makeText(applicationContext, "Échec de la suppression: $errorBody", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                            Toast.makeText(applicationContext, "Erreur: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
        }
    }
}