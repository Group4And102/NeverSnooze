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

    private var soundSelectedListener: ((String) -> Unit)? = null
    private var mediaPlayer: MediaPlayer? = null

    // Sample sound options
    private val soundOptions = listOf(
        "Radar", "Chimes", "Ripples")

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
        recyclerView.adapter = SoundAdapter(soundOptions) { soundName ->
            // Play sound preview
            playSoundPreview(soundName)

            // Invoke callback with the selected sound
            soundSelectedListener?.invoke(soundName)

            // Dismiss dialog
            dismiss()
        }

        val titleView = view.findViewById<TextView>(R.id.titleText)
        titleView.text = "Select Sound"

        return view
    }

    private fun playSoundPreview(soundName: String) {
        // In a real app, you would play the actual sound file
        // For this example, we'll just use a default sound

        // Release previous MediaPlayer if any
        mediaPlayer?.release()

        // Create new MediaPlayer
        mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        // Release MediaPlayer resources
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    fun setOnSoundSelectedListener(listener: (String) -> Unit) {
        soundSelectedListener = listener
    }

    /**
     * Adapter for displaying sound options
     */
    private inner class SoundAdapter(
        private val sounds: List<String>,
        private val onSoundSelected: (String) -> Unit
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
            holder.radioButton.text = sound

            holder.itemView.setOnClickListener {
                onSoundSelected(sound)
            }

            holder.radioButton.setOnClickListener {
                onSoundSelected(sound)
            }
        }

        override fun getItemCount() = sounds.size
    }

    companion object {
        fun newInstance(): SoundSelectorDialog {
            return SoundSelectorDialog()
        }
    }
}