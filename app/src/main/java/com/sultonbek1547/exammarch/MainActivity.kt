package com.sultonbek1547.exammarch

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sultonbek1547.exammarch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var handler: Handler
    private lateinit var referenceMediaState: DatabaseReference
    private lateinit var referenceSeekbarPosition: DatabaseReference
    private var mediaPlayerState: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        referenceMediaState = Firebase.database.getReference("media_player_state")
        referenceSeekbarPosition = Firebase.database.getReference("media_player_position")
        mediaPlayer = MediaPlayer()
        handler = Handler(Looper.getMainLooper())


        /** Media player REALTIME Start or Stop*/
        referenceMediaState.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mediaPlayerState = snapshot.getValue<Boolean>() == true
                if (mediaPlayerState) {
                    mediaPlayer.start()
                    binding.btnStart.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                } else {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        binding.btnStart.setImageResource(R.drawable.baseline_play_circle_outline_24)

                    }


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        /** Media player REALTIME position(progress) change listener*/
        referenceSeekbarPosition.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<Int>()
                if (value != null) {
                    val position = (value * mediaPlayer.duration / 100)
                    mediaPlayer.seekTo(position)
                }

            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
        mediaPlayer.setDataSource(this, uri)
        mediaPlayer.prepare()
        binding.seekBar.max = mediaPlayer.duration


        /** starting || stopping song */
        binding.btnStart.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayerState = false
                binding.btnStart.setImageResource(R.drawable.baseline_play_circle_outline_24)
            } else {
                mediaPlayerState = true
                binding.btnStart.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                binding.seekBar.progress = mediaPlayer.currentPosition
            }
            referenceMediaState.setValue(mediaPlayerState)
        }

        /** changing music using seekBar */
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    // giving as percentage
                    referenceSeekbarPosition.setValue((p1 * 100 / mediaPlayer.duration))

                    if (mediaPlayer.currentPosition / 1000 % 60 < 10) binding.tvCount.text =
                        "${mediaPlayer.currentPosition / 1000 / 60}:0${mediaPlayer.currentPosition / 1000 % 60}"
                    else binding.tvCount.text =
                        "${mediaPlayer.currentPosition / 1000 / 60}:${mediaPlayer.currentPosition / 1000 % 60}"

                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })


        if (mediaPlayer.isPlaying) {
            binding.btnStart.setImageResource(R.drawable.baseline_pause_circle_outline_24)
            /**volume...*/
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val progress = (currentVolume.toFloat() / maxVolume * 100).toInt()
            binding.volumeSeekbar.progress = progress

        }

        /** tv progress */
        if (mediaPlayer.currentPosition / 1000 % 60 < 10) binding.tvCount.text =
            "${mediaPlayer.currentPosition / 1000 / 60}:0${mediaPlayer.currentPosition / 1000 % 60}"
        else binding.tvCount.text =
            "${mediaPlayer.currentPosition / 1000 / 60}:${mediaPlayer.currentPosition / 1000 % 60}"


        /** tv_max */
        binding.tvMax.text =
            "${mediaPlayer.duration / 1000 / 60}:${mediaPlayer.duration / 1000 % 60}"


        /** dealing with volume */
        binding.volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val volume: Int = p1 * maxVolume / 100 // Scale the progress to the volume range
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })


        /** changing seekBar as music goes */
        handler.postDelayed(object : Runnable {
            override fun run() {
                binding.seekBar.progress = mediaPlayer.currentPosition
                if (mediaPlayer.currentPosition / 1000 % 60 < 10) binding.tvCount.text =
                    "${mediaPlayer.currentPosition / 1000 / 60}:0${mediaPlayer.currentPosition / 1000 % 60}"
                else binding.tvCount.text =
                    "${mediaPlayer.currentPosition / 1000 / 60}:${mediaPlayer.currentPosition / 1000 % 60}"

                handler.postDelayed(this, 1000)
            }
        }, 1000)


        /** when Music ends */
        mediaPlayer.setOnCompletionListener {
            it.seekTo(0)
            it.pause()
            binding.btnStart.setImageResource(R.drawable.baseline_play_circle_outline_24)
            referenceMediaState.setValue(false)
        }

    }
}