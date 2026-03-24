package ru.hopes.randomizer

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Менеджер для управления диалоговыми окнами в приложении.
 *
 * Инкапсулирует логику создания и отображения [AlertDialog],
 * предоставляя удобный API для взаимодействия с пользователем.
 *
 * @param context Контекст приложения, используемый для создания диалогов.
 */
class DialogManager(private val context: Context) {

    /**
     * Отображает диалог выбора действия с опциями: "Добавить", "Удалить", "Отмена".
     *
     * @param options Список текущих опций для проверки на пустоту.
     * @param onAdd Callback, вызываемый при выборе действия "Добавить".
     * @param onRemove Callback, вызываемый при выборе действия "Удалить".
     */
    fun showOptionsDialog(
        options: List<String>,
        onAdd: () -> Unit,
        onRemove: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle("Что будем делать?")
            .setPositiveButton("Добавить") { _, _ -> onAdd() }
            .setNegativeButton("Удалить") { _, _ -> onRemove() }
            .setNeutralButton("Отмена", null)
            .show()
    }

    /**
     * Отображает диалог выбора элемента для добавления из списка.
     *
     * @param options Список доступных опций для выбора.
     * @param onConfirm Callback, вызываемый при подтверждении выбора.
     *                  Передаёт выбранную опцию в качестве параметра.
     */
    fun showAddDialog(options: List<String>, onConfirm: (String) -> Unit) {
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle("Кого будем добавлять?")
            .setItems(
                options.toTypedArray()
            ) { _, which ->
                onConfirm(options[which])
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    /**
     * Отображает диалог множественного выбора элементов для удаления.
     *
     * @param options Список опций для выбора на удаление.
     * @param onConfirm Callback, вызываемый при подтверждении удаления.
     *                  Передаёт список выбранных элементов для удаления.
     */
    fun showRemoveDialog(
        options: List<String>,
        onConfirm: (List<String>) -> Unit
    ) {
        if (options.isEmpty()) {
            Toast.makeText(context, "Список пуст", Toast.LENGTH_SHORT).show()
            return
        }

        val checkedItems = BooleanArray(options.size)

        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle("Кого будем удалять?")
            .setMultiChoiceItems(
                options.toTypedArray(),
                checkedItems
            ) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Удалить") { dialog, _ ->
                val toRemove = options.filterIndexed { index, _ ->
                    checkedItems[index]
                }

                if (toRemove.isNotEmpty()) {
                    onConfirm(toRemove)
                } else {
                    Toast.makeText(context, "Ничего не выбрано", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    /**
     * Отображает диалог ввода нового элемента для добавления.
     *
     * @param existingOptions Список существующих опций для проверки дубликатов.
     * @param onConfirm Callback, вызываемый при подтверждении добавления.
     *                  Передаёт новый элемент в качестве параметра.
     */
    fun showAddDialogWithInput(
        existingOptions: List<String>,
        onConfirm: (String) -> Unit
    ) {
        val editText = EditText(context).apply {
            hint = context.getString(ru.hopes.randomizer.R.string.enter_element_name)
            setPadding(48, 32, 48, 32)
        }

        val dialog = AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(ru.hopes.randomizer.R.string.add_element)
            .setView(editText)
            .setPositiveButton(ru.hopes.randomizer.R.string.add) { _, _ ->
                val newElement = editText.text.toString().trim()
                if (newElement.isNotEmpty()) {
                    onConfirm(newElement)
                } else {
                    Toast.makeText(context, "Введите имя элемента", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(ru.hopes.randomizer.R.string.cancel, null)
            .create()

        dialog.show()
        editText.requestFocus()

        // Показываем клавиатуру после отображения диалога
        editText.postDelayed({
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(editText, 0)
        }, 100)
    }

    /**
     * Фабричный метод для создания экземпляра [DialogManager].
     *
     * @param activity Активность, которая будет использоваться как контекст.
     * @return Новый экземпляр [DialogManager].
     */
    companion object {
        fun create(activity: AppCompatActivity): DialogManager {
            return DialogManager(activity)
        }
    }
}
