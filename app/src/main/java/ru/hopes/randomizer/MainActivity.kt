package ru.hopes.randomizer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.hopes.randomizer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var dialogManager: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        dialogManager = DialogManager.create(this)

        setWindowInsetsListener()
        setupClickListeners()
    }

    private fun setWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            wheelView.setOptions(mutableListOf("Artur", "Sergey", "Arshak", "Dima", "Yuri"))

            spinButton.setOnClickListener {
                if (wheelView.options.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Добавьте сначала хотя бы один элемент",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                spinButton.isEnabled = false
                wheelView.spinWheel { winner ->
                    winnerTextView.setupWinnerWithAnimation(winner)
                    spinButton.isEnabled = true
                }
                winnerTextView.cleanWithAnimation()
                    }
            }

            addButton.setOnClickListener {
                val options = wheelView.options
                dialogManager.showOptionsDialog(
                    options = options,
                    onAdd = { showAddDialog(options) },
                    onRemove = { showRemoveDialog(options) }
                )
            }
        }
    }

    private fun showAddDialog(options: List<String>) {
        dialogManager.showAddDialogWithInput(options) { newElement ->
            val currentOptions = binding.wheelView.options.toMutableList()
            if (!currentOptions.contains(newElement)) {
                currentOptions.add(newElement)
                binding.wheelView.setOptions(currentOptions)
            } else {
                Toast.makeText(this, R.string.already_exists, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRemoveDialog(options: List<String>) {
        dialogManager.showRemoveDialog(options) { toRemove ->
            binding.wheelView.removeOptions(toRemove)
        }
    }
}