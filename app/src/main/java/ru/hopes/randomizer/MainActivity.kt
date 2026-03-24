package ru.hopes.randomizer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.hopes.randomizer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var dialogManager: DialogManager
    private lateinit var vibrator: Vibrator
    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null

    // Длительность долгого тапа для активации скрытого меню (в миллисекундах)
    private val longPressDuration = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        dialogManager = DialogManager.create(this)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        setWindowInsetsListener()
        setupClickListeners()
        setupLongClickListener()
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

    @Suppress("ClickableViewAccessibility")
    private fun setupLongClickListener() {
        longPressRunnable = Runnable {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            showRiggedDialog()
        }

        binding.wheelView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.postDelayed(longPressRunnable!!, longPressDuration)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPressRunnable!!)
                    false
                }
                else -> false
            }
        }
    }

    private fun showRiggedDialog() {
        val wheelView = binding.wheelView
        dialogManager.showRiggedModeDialog(
            options = wheelView.options,
            currentWeights = wheelView.getWeights(),
            currentRiggedWinner = wheelView.getRiggedWinner(),
            isRiggedModeEnabled = wheelView.isRiggedModeEnabled()
        ) { weights, riggedWinner, enabled ->
            wheelView.setWeights(weights ?: emptyMap())
            wheelView.setRiggedWinner(riggedWinner)
            wheelView.setRiggedMode(enabled)

            val statusText = when {
                !enabled -> "Режим подкрутки выключен"
                riggedWinner != null -> "Подкрутка: победитель — $riggedWinner"
                weights?.isNotEmpty() == true -> "Подкрутка: режим весов активен"
                else -> "Режим подкрутки включён"
            }
            Toast.makeText(this, statusText, Toast.LENGTH_SHORT).show()
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