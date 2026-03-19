package ru.hopes.randomizer

import android.content.Context
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
        if (options.isEmpty()) {
            Toast.makeText(context, "Список пуст", Toast.LENGTH_SHORT).show()
            return
        }

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
