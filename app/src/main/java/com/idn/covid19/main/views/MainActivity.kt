package com.idn.covid19.main.views

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.idn.covid19.R
import com.idn.covid19.databinding.ActivityMain2Binding
import com.idn.covid19.main.models.CovidModel
import com.idn.covid19.main.viewmodels.WorldViewModel
import kotlinx.android.synthetic.main.bottomsheet_reload.view.*
import retrofit2.HttpException
import java.text.DecimalFormat
import java.text.NumberFormat
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    private lateinit var worldBinding: ActivityMain2Binding
    private lateinit var vmWorld: WorldViewModel

    var dialogLoading: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
    }

    private fun initBinding() {
        worldBinding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        vmWorld = ViewModelProviders.of(this).get(WorldViewModel::class.java)
        worldBinding.worldData = vmWorld

        fetchData()

        vmWorld.cekWorldResponse.observe(this, Observer {
            showDataUI(it)
        })

        vmWorld.error.observe(this, Observer {
            handlingError(it)
        })
    }

    private fun fetchData() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        val message = dialogView.findViewById<TextView>(R.id.txt_dialog_message)
        message.text = getString(R.string.loading)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialogLoading = builder.create()
        dialogLoading!!.show()
        vmWorld.getWorld()
    }

    private fun showDataUI(it: CovidModel?) {
        dialogLoading?.dismiss()
        val formatter: NumberFormat = DecimalFormat("#,###")

        worldBinding.txtTotalConfirm.text = formatter.format(it?.global?.totalConfirmed?.toDouble())
        worldBinding.txtTotalRecovered.text = formatter.format(it?.global?.totalRecovered?.toDouble())
        worldBinding.txtTotalDeaths.text = formatter.format(it?.global?.totalDeaths?.toDouble())
    }

    private fun handlingError(it: Throwable?) {
        Log.d("debug ", "handlingError: " + it.toString())
        if (it is HttpException) {
            when (it.code()) {
                HttpsURLConnection.HTTP_BAD_REQUEST -> {
                    onReloadData(getString(R.string.something_went_wrong))
                }
                HttpsURLConnection.HTTP_FORBIDDEN -> {
                    onReloadData(getString(R.string.something_went_wrong))
                }
                HttpsURLConnection.HTTP_INTERNAL_ERROR -> {
                    onReloadData(getString(R.string.something_went_wrong))
                }
                else -> {
                    onReloadData(getString(R.string.something_went_wrong))
                }
            }
        }
        onReloadData(getString(R.string.check_internet))
    }

    fun onReloadData(message: String) {
        val view = layoutInflater.inflate(R.layout.bottomsheet_reload, null)
        val dialog = BottomSheetDialog(this, R.style.BaseBottomSheetDialog)
        view.close_reload.setOnClickListener {
            dialog.dismiss()
        }

        view.btn_reload.setOnClickListener {
            dialog.dismiss()
            fetchData()
        }

        view.txt_wrong.text = message

        dialog.setContentView(view)
        dialog.show()
    }

    fun moveToCountry(view: View){
        startActivity(Intent(this, ListCountryActivity::class.java))
    }
}