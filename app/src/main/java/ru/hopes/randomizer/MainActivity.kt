package ru.hopes.randomizer

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import ru.hopes.randomizer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setWindowInsetsListener()
        createAlertDialog()

    }

    private fun setWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun createAlertDialog() {
        with(binding) {

            spinButton.setOnClickListener {
                spinButton.isEnabled = false
                wheelView.spinWheel { winner ->
                    winnerTextView.setupWinnerWithAnimation(winner)
                    spinButton.isEnabled = true
                }
                winnerTextView.cleanWithAnimation()
            }
            wheelView.setOptions(mutableListOf("Artur", "Sergey", "Arshak", "Dima", "Yuri"))

            addButton.setOnClickListener {

                val dialogState: MutableStateFlow<Boolean> = MutableStateFlow(true)
                val options = wheelView.options
                val checkedItems = BooleanArray(options.size)

                if (dialogState.value) {

                    AlertDialog.Builder(this@MainActivity)
                        .setCancelable(false)
                        .setTitle("Что будем делать?")
                        .setNegativeButton(
                            "Удалить",
                            DialogInterface.OnClickListener { _, n ->
                                AlertDialog.Builder(this@MainActivity)
                                    .setCancelable(false)
                                    .setTitle("Кого будем удалять?")
                                    .setMultiChoiceItems(
                                        options.toTypedArray(), checkedItems,
                                        DialogInterface.OnMultiChoiceClickListener { _, which, isChecked ->
                                            checkedItems[which] = isChecked
                                        })
                                    .setPositiveButton("Удалить") { dialog, _ ->
                                        val toRemove = mutableListOf<String>()
                                        checkedItems.forEachIndexed { index, isChecked ->
                                            if (isChecked) {
                                                toRemove.add(options[index])
                                            }
                                        }

                                        if (toRemove.isNotEmpty()) {
                                            wheelView.removeOptions(toRemove)
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Удалено: ${toRemove.size}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .setNegativeButton("Отмена", null)
                                    .show()
                            }
                        )
                        .setNeutralButton(
                            "Отмена",
                            null
                        )
                        .setPositiveButton(
                            "Добавить",
                            DialogInterface.OnClickListener { _, n ->
                                AlertDialog.Builder(this@MainActivity)
                                    .setCancelable(false)
                                    .setTitle("Кого будем добавлять?")

                                    .setPositiveButton("Удалить") { dialog, _ ->
                                        val toRemove = mutableListOf<String>()
                                        checkedItems.forEachIndexed { index, isChecked ->
                                            if (isChecked) {
                                                toRemove.add(options[index])
                                            }
                                        }

                                        if (toRemove.isNotEmpty()) {
                                            wheelView.removeOptions(toRemove)
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Удалено: ${toRemove.size}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .setNegativeButton("Отмена", null)
                                    .show()
                            }
                        )
                }
            }
        }
    }
}