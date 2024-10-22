package com.example.api7

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import android.app.AlertDialog
import android.content.Intent
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getDATA()
    }
    override fun onResume() {
        super.onResume()
        getDATA()
    }

    fun getDATA(){
        val list_view = findViewById<ListView>(R.id.listView)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://apiyes.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.getPC()

        call.enqueue(object : Callback<List<Ordinateur>>{
            override fun onResponse(call: Call<List<Ordinateur>>, response: Response<List<Ordinateur>>) {
                if (response.isSuccessful){
                    val ordinateur = response.body() ?: emptyList()

                    val pc_names = ArrayList<String>()

                    for (pc in ordinateur){
                        pc_names.add("${pc.name} - ${pc.price} MAD")
                    }

                    val adapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_list_item_1 , pc_names)
                    list_view.adapter = adapter

                    list_view.setOnItemClickListener{parent, view, position, id ->
                        val selectedPC = ordinateur[position]
                        val intent = Intent(this@MainActivity, up_and_del::class.java).also {
                            it.putExtra("name", selectedPC.name)
                            it.putExtra("prix", selectedPC.price)
                            it.putExtra("image", selectedPC.image)
                            it.putExtra("switch", selectedPC.haveFP)
                            it.putExtra("id", selectedPC.id)
                        }
                        startActivity(intent)
                    }
                }
                else{
                    Toast.makeText(this@MainActivity, "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Ordinateur>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Echec de la cennexion", Toast.LENGTH_SHORT).show()
            }
        })


//    Ajouter PC:

        val ed_name = findViewById<EditText>(R.id.editTextName)
        val ed_price = findViewById<EditText>(R.id.editTextPrice)
        val ed_image = findViewById<EditText>(R.id.editTextImage)
        val sw_haveFP = findViewById<Switch>(R.id.switchHaveFP)
        val add_btn = findViewById<Button>(R.id.buttonAdd)

        add_btn.setOnClickListener {
            val name = ed_name.text.toString().trim()
            val price = ed_price.text.toString().trim()
            val image = ed_image.text.toString().trim()
            val haveFP = sw_haveFP.isChecked

            if (name.isEmpty() || price.isEmpty() || image.isEmpty()) {
                Toast.makeText(this@MainActivity, "Remplir tous les champs", Toast.LENGTH_SHORT).show()
            } else {
                val dialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle("Ajouter PC")
                    .setMessage("Voulez-vous vraiment ajouter ce PC?")
                    .setPositiveButton("Ok") { dialog, _ ->


                        val prix = price.toDouble()

                        val PC = Ordinateur(0, name, prix, haveFP, image)

                        apiService.addPC(PC).enqueue(object : Callback<AddResponse> {
                            override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                                if (response.isSuccessful) {
                                    val addResponse = response.body()
                                    if (addResponse != null) {
                                        Toast.makeText(applicationContext, addResponse.message, Toast.LENGTH_LONG).show()
                                        if (addResponse.code == 1) {
                                            getDATA()  // Refresh the list
                                        }
                                    }
                                } else {
                                    Toast.makeText(applicationContext, "Failed to add PC", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                            }
                        })
                        dialog.dismiss()
                    }
                    .setNegativeButton("Annuler") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
            }
        }




//        Search


        val search = findViewById<Button>(R.id.buttonSearch)

        search.setOnClickListener {
            val searchQuery = ed_name.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                apiService.getPC().enqueue(object : Callback<List<Ordinateur>> {
                    override fun onResponse(call: Call<List<Ordinateur>>, response: Response<List<Ordinateur>>) {
                        if (response.isSuccessful) {
                            val ordinateur = response.body() ?: emptyList()

                            val filteredResults = ordinateur.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }

                            val pc_names = ArrayList<String>()
                            for (pc in filteredResults) {
                                pc_names.add("${pc.name} - ${pc.price} MAD")
                            }

                            val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, pc_names)
                            list_view.adapter = adapter

                            if (pc_names.isEmpty()) {
                                Toast.makeText(this@MainActivity, "No results found", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<Ordinateur>>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@MainActivity, "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
        }


    }
}