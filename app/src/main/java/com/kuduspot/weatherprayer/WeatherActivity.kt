package com.kuduspot.weatherprayer


import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_weather.*
import org.json.JSONObject
import android.view.WindowManager
import android.widget.Toast
import com.github.matteobattilana.weather.PrecipType
import com.github.matteobattilana.weather.WeatherViewSensorEventListener
import im.delight.android.location.SimpleLocation
import com.mikhaellopez.circularprogressbar.CircularProgressBar




class WeatherActivity : AppCompatActivity(),AdapterView.OnItemSelectedListener {


    lateinit var weatherSensor: WeatherViewSensorEventListener

    //var particleAnim:ParticleSystem?=null

    val tumTahminBilgisi=ArrayList<Tahmin>()
    val DERECE="°"
    var myApiID="d0f0b3abdba68b412555a6651a304d99"
    var sehir ="Ankara"
    var location: SimpleLocation? = null
    var latitude: String? = null
    var longitude: String? = null

    override fun onResume() {
        super.onResume()

        if(tvKonumSehir.visibility==View.VISIBLE)
            spnSehir.visibility=View.INVISIBLE
        else
            spnSehir.visibility=View.VISIBLE

    }
    fun windDirection(degree:Double):String{
        var sonuc=""
        when(degree){
            0.0 -> sonuc="Kuzey"
            in 1.0..89.9-> sonuc="Kuzeydoğu"
            90.0-> sonuc="Doğu"
            in 90.1..179.9-> sonuc="Güneydoğu"
            180.0-> sonuc="Güney"
            in 180.1..269.9-> sonuc="Güneybatı"
            270.0-> sonuc="Batı"
            in 270.1..359.9-> sonuc="Kuzeybatı"
            360.0-> sonuc="Kuzey"
        }
        return sonuc
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        //Spinner'dan şehir seçilecek. Şehir bilgisi ordan gelecek.
        ArrayAdapter.createFromResource(
                this,
                R.array.spinner_sehir,
                R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spnSehir.adapter = adapter
            //spnSehir.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        }

        spnSehir.setOnItemSelectedListener(this@WeatherActivity)
        spnSehir.setSelection(0)

        tumTahminBilgisi.clear()
        verileriGetir(sehir,"","")
        weatherSensor = WeatherViewSensorEventListener(this, weather_view)


        tvKonumSehir.setOnClickListener{

            spnSehir.visibility=View.VISIBLE
            spnSehir.performClick()
            it.visibility=View.INVISIBLE

        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (position == 0) {


            location = SimpleLocation(this)

            if(!location!!.hasLocationEnabled()){

                Toast.makeText(this, "GPS Kapalı", Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this)
                spnSehir.setSelection(1)

            }else{

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),60)
                }else{

                    location = SimpleLocation(this)
                    latitude = String.format("%.6f", location?.latitude)
                    longitude = String.format("%.6f", location?.longitude)

                    verileriGetir("",latitude!!,longitude!!)
                    tumTahminBilgisi.clear()
                    rvTahminler.adapter?.notifyItemRangeRemoved(0,tumTahminBilgisi.size)

                    view?.visibility=View.INVISIBLE
                    tvKonumSehir.visibility=View.VISIBLE
                }
            }

        } else {

            var secilenSehir = parent?.getItemAtPosition(position).toString()
            verileriGetir(secilenSehir,"","")
            tumTahminBilgisi.clear()
            rvTahminler.adapter?.notifyItemRangeRemoved(0,tumTahminBilgisi.size)
        }

    }

    fun tarihFormat(tarih:String):String{

        var sene=tarih.substring(0,4)
        var ay=ayFormat(tarih.substring(5,7))
        var gun=tarih.substring(8,10)
        var yenitarih=gun+" "+ay+" "+sene
        return yenitarih
    }

    fun ayFormat(ay:String):String{
        when(ay){
            "01"->return "Ocak"
            "02"->return "Şubat"
            "03"->return "Mart"
            "04"->return "Nisan"
            "05"->return "Mayıs"
            "06"->return "Haziran"
            "07"->return "Temmuz"
            "08"->return "Ağustos"
            "09"->return "Eylül"
            "10"->return "Ekim"
            "11"->return "Kasım"
            "12"->return "Aralık"
            else->return "Ocak"
        }

    }

    fun verileriGetir(sehir:String,lat:String,lon:String){

        var havaJsonApi=""
        if(sehir.isNullOrEmpty()){
            if(!lat.isNullOrEmpty()&&!lon.isNullOrEmpty()){
                havaJsonApi="https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&lang=tr&units=metric&appid=$myApiID"
            }else{
                Toast.makeText(this,
                                "Konumunuz Belirlenemedi. Lütfen konum ayarlarınızı kontrol edin.",
                                Toast.LENGTH_LONG).show()
                spnSehir.setSelection(1)
            }
        }else{
            havaJsonApi="https://api.openweathermap.org/data/2.5/forecast?q=$sehir&lang=tr&units=metric&appid=$myApiID"
        }

        val havaDurumu= JsonObjectRequest(Request.Method.GET, havaJsonApi, null,
                object : Response.Listener<JSONObject>{
                    override fun onResponse(response: JSONObject?) {

                        var havaDurumuListe=response?.getJSONArray("list")
                        var city=response?.getJSONObject("city")
                        var cityName=city?.getString("name")
                        tvKonumSehir.text=cityName
                        for(i in 0..(havaDurumuListe!!.length()-1)){

                            var currentForecast=havaDurumuListe?.getJSONObject(i)
                            var tarih=currentForecast?.getString("dt_txt")
                            var saatBilgisi=tarih?.substring(tarih?.length-8,tarih?.length-3)

                            var main=currentForecast?.getJSONObject("main")
                            var wind=currentForecast?.getJSONObject("wind")
                            var clouds=currentForecast?.getJSONObject("clouds")

                            var humidity=main?.getInt("humidity")
                            var windSpeed=wind?.getInt("speed")
                            var windDeg=wind?.getDouble("deg")
                            var temp=main?.getInt("temp")
                            var weather=currentForecast?.getJSONArray("weather")
                            var weatherMain=weather?.getJSONObject(0)?.getString("main")
                            var weatherDescription = weather?.getJSONObject(0)?.getString("description")
                            var pressure=main?.getInt("pressure")
                            var cloudsAll=clouds?.getInt("all")

                            if(i==0) {
                                tvSicaklikTahmin.text=temp.toString()+DERECE
                                tvRuzgarYon.text=windDirection(windDeg!!)
                                tvRuzgarHiz.text=windSpeed.toString()+" m/s"
                                tvAciklama.text=weatherDescription.toString().replaceFirst(weatherDescription!!.first(),weatherDescription?.first()!!.toUpperCase())
                                tvUpdated.text="Son Güncelleme "+saatBilgisi
                                //tvTarih.text=tarih?.substring(0,tarih?.length-3)
                                tvTarih.text=tarihFormat(tarih!!)
                                tvBasinc.text=pressure.toString()+" mBar"
                                tvBulut.text="  "+cloudsAll.toString()+"%"

                                val circularProgressBar = findViewById(R.id.pbNemSeviyesi) as CircularProgressBar
                                circularProgressBar.color = ContextCompat.getColor(this@WeatherActivity, R.color.cardview_light_background)
                                circularProgressBar.backgroundColor = ContextCompat.getColor(this@WeatherActivity, R.color.dropdown_default_divider_color)
                                val animationDuration = 2500 // 2500ms = 2,5s
                                circularProgressBar.setProgressWithAnimation(humidity!!.toFloat(), animationDuration) // Default duration = 1500ms
                                tvNemSeviyesi.text=humidity.toString()+"%"


                                var iconView:String = ""
                                when(weatherMain) {
                                    "Clear" -> {
                                        constLayout.setBackgroundColor(getColor(R.color.Clear))
                                        iconView = getString(R.string.wi_forecast_io_clear_day)
                                        weatherIconView.setIconResource(iconView)
                                        // clear FLAG_TRANSLUCENT_STATUS flag:
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                                        // finally change the color
                                        window.setStatusBarColor(getColor(R.color.Clear))

                                        weatherSensor.stop()
                                        weather_view.setWeatherData(PrecipType.CLEAR)
                                        weatherSensor.start()

                                    }
                                    "Rain" -> {
                                        constLayout.setBackgroundColor(getColor(R.color.Rain))
                                        iconView = getString(R.string.wi_rain)
                                        weatherIconView.setIconResource(iconView)
                                        // clear FLAG_TRANSLUCENT_STATUS flag:
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                                        // finally change the color
                                        window.setStatusBarColor(getColor(R.color.Rain))

                                        //Yağmur Efekti
                                        weatherSensor.stop()
                                        weather_view.setWeatherData(PrecipType.RAIN)
                                        weatherSensor.start()

                                    }
                                    "Clouds" -> {
                                        constLayout.setBackgroundColor(getColor(R.color.Clouds))
                                        iconView = getString(R.string.wi_cloudy)
                                        weatherIconView.setIconResource(iconView)
                                        // clear FLAG_TRANSLUCENT_STATUS flag:
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                                        // finally change the color
                                        window.setStatusBarColor(getColor(R.color.Clouds))

                                        weatherSensor.stop()
                                        weather_view.setWeatherData(PrecipType.CLEAR)
                                        weatherSensor.start()

                                    }
                                    "Snow" -> {
                                        constLayout.setBackgroundColor(getColor(R.color.Snow))
                                        iconView = getString(R.string.wi_snow)
                                        weatherIconView.setIconResource(iconView)
                                        // clear FLAG_TRANSLUCENT_STATUS flag:
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                                        // finally change the color
                                        window.setStatusBarColor(getColor(R.color.Snow))

                                        //Kar efekti
                                        weatherSensor.stop()
                                        weather_view.setWeatherData(PrecipType.SNOW)
                                        weatherSensor.start()
                                    }
                                    else -> {
                                        constLayout.setBackgroundColor(getColor(R.color.Clear))
                                        iconView = getString(R.string.wi_wu_clear)
                                        weatherIconView.setIconResource(iconView)
                                        // clear FLAG_TRANSLUCENT_STATUS flag:
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                                        // finally change the color
                                        window.setStatusBarColor(getColor(R.color.Clear))

                                        weatherSensor.stop()
                                        weather_view.setWeatherData(PrecipType.CLEAR)
                                        weatherSensor.start()

                                    }

                                }
                            } else {
                                var tahminEkle=Tahmin(saatBilgisi!!,temp.toString()+DERECE,weatherMain.toString())
                                tumTahminBilgisi.add(tahminEkle)
                            }

                        }

                        var myAdapter=TahminAdapter(this@WeatherActivity.tumTahminBilgisi)

                        rvTahminler.adapter=myAdapter
                        var linearLayoutManager=LinearLayoutManager(this@WeatherActivity,LinearLayoutManager.HORIZONTAL,false)
                        rvTahminler.layoutManager=linearLayoutManager



                    }},
                object : Response.ErrorListener{
                    override fun onErrorResponse(error: VolleyError?) {

                    }
                })

        MySingleton.getInstance(this)?.addToRequestQueue(havaDurumu)
    }


}
