package com.example.neversnooze

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Dialog for selecting alarm sounds
 */
class SoundSelectorDialog : BottomSheetDialogFragment() {

    private var soundSelectedListener: ((Pair<String, String>) -> Unit)? = null
    private var mediaPlayer: MediaPlayer? = null

    private val soundOptions = listOf(
        SoundOption("Chimes", "chimes"),
        SoundOption("Over The Horizon", "over_the_horizon"),
        SoundOption("Never Ending Quest", "never_ending_quest")
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_sound_selector, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.soundRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = SoundAdapter(soundOptions) { soundOption ->
            playSoundPreview(soundOption.fileName)
            soundSelectedListener?.invoke(soundOption.label to soundOption.fileName)
            dismiss()
        }

        val titleView = view.findViewById<TextView>(R.id.titleText)
        titleView.text = getString(R.string.select_sound)

        return view
    }

    private fun playSoundPreview(fileName: String) {
        mediaPlayer?.release()
        val resId = resources.getIdentifier(fileName, "raw", requireContext().packageName)
        if (resId != 0) {
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.start()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    fun setOnSoundSelectedListener(listener: (Pair<String, String>) -> Unit) {
        soundSelectedListener = listener
    }

    /**
     * Adapter for displaying sound options
     */
    private inner class SoundAdapter(
        private val sounds: List<SoundOption>,
        private val onSoundSelected: (SoundOption) -> Unit
    ) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

        inner class SoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val radioButton: RadioButton = view.findViewById(R.id.radioSound)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sound, parent, false)
            return SoundViewHolder(view)
        }

        override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
            val sound = sounds[position]
            holder.radioButton.text = sound.label

            holder.itemView.setOnClickListener {
                onSoundSelected(sound)
            }

            holder.radioButton.setOnClickListener {
                onSoundSelected(sound)
            }
        }

        override fun getItemCount() = sounds.size
    }

    data class SoundOption(val label: String, val fileName: String)

    companion object {
        fun newInstance(): SoundSelectorDialog {
            return SoundSelectorDialog()
        }
    }
}
