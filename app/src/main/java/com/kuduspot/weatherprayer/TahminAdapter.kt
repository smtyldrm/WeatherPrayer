package com.kuduspot.weatherprayer

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.tek_satir_horizontal.view.*

class TahminAdapter (tumTahmin:ArrayList<Tahmin>): RecyclerView.Adapter<TahminAdapter.TahminViewHolder>() {

    var tahmin=tumTahmin

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TahminViewHolder {

        var inflater= LayoutInflater.from(parent.context)
        var tekSatirTahmin=inflater.inflate(R.layout.tek_satir_horizontal,parent,false)

        return TahminViewHolder(tekSatirTahmin)
    }

    override fun getItemCount(): Int {
        return tahmin.size
    }

    override fun onBindViewHolder(holder: TahminViewHolder, position: Int) {

        var curBurcBilgisi=tahmin.get(position)

        holder?.setData(curBurcBilgisi,position)

    }

    class TahminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var teksatirTahmin=itemView as CardView


        var saat=teksatirTahmin.tvSaat
        var sicaklik=teksatirTahmin.tvSicaklikTahmin
        var weatherIcon=teksatirTahmin.weatherIconView


        fun setData(curTahminBilgisi: Tahmin, position: Int) {
            saat.text=curTahminBilgisi.Saat
            sicaklik.text=curTahminBilgisi.Sicaklik

            var iconSec=chooseIconView(curTahminBilgisi.WeatherIconString,itemView)
            weatherIcon.setIconResource(iconSec)

        }
        fun chooseIconView(weatherIconString: String, item: View): String {

            var sonuc=""
            when(weatherIconString){
                "Clear"->sonuc= item.context.getString(R.string.wi_forecast_io_clear_day)
                "Rain"->sonuc=item.context.getString(R.string.wi_rain)
                "Clouds"->sonuc=item.context.getString(R.string.wi_cloudy)
                "Snow"->sonuc=item.context.getString(R.string.wi_snow)
                else ->sonuc=item.context.getString(R.string.wi_wu_clear)
            }
            return sonuc
        }



    }
}