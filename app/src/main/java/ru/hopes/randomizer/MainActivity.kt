package ru.hopes.randomizer

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.hopes.randomizer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setWindowInsetsListener()

        with(binding) {
            addButton.setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Показал писюн?")
                    .setCancelable(false)
                    .setNegativeButton(
                        "Хуесос!",
                        DialogInterface.OnClickListener { _, n ->
                            Toast.makeText(
                                this@MainActivity,
                                "Хуесос $n",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                    .set
                    .setNeutralButton(
                        "не молодец!",
                        DialogInterface.OnClickListener { _, n ->
                            Toast.makeText(
                                this@MainActivity,
                                "не молодец! $n",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                    .setPositiveButton(
                        "Молодец!",
                        DialogInterface.OnClickListener { _, n ->
                            Toast.makeText(
                                this@MainActivity,
                                "Молодец $n",
                                Toast.LENGTH_LONG
                            ).show()
                        }).create().show()
            }

            spinButton.setOnClickListener {
                spinButton.isEnabled = false
                wheelView.spinWheel { winner ->
                    winnerTextView.setupWinnerWithAnimation(winner)
                    spinButton.isEnabled = true
                }
                winnerTextView.cleanWithAnimation()
            }
            wheelView.setOptions(listOf("Artur", "Sergey", "Arshak", "Dima", "Yuri"))
        }

    }


    private fun setWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}