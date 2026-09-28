package com.example.arrowescape.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arrowescape.R
import com.example.arrowescape.logic.GameManager
import com.example.arrowescape.model.DotGridLevel

class LevelsAdapter(
    private val levels: List<DotGridLevel>,
    private val currentLevelId: Int,
    private val onLevelSelected: (DotGridLevel) -> Unit
) : RecyclerView.Adapter<LevelsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: FrameLayout = view.findViewById(R.id.cardLevelContainer)
        val tvNum: TextView = view.findViewById(R.id.tvItemLevelNum)
        val tvCheck: TextView = view.findViewById(R.id.tvCheckIcon)
        val tvLock: TextView = view.findViewById(R.id.tvLockIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_level, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val level = levels[position]
        val isCompleted = GameManager.isLevelCompleted(level.id)
        val isUnlocked = GameManager.isLevelUnlocked(level.id)
        val isCurrent = level.id == currentLevelId

        holder.tvNum.text = level.id.toString()

        val bgDrawable = GradientDrawable().apply {
            cornerRadius = 32f
            when {
                isCompleted -> {
                    // Completed levels: Green box
                    setColor(Color.parseColor("#065F46"))
                    setStroke(if (isCurrent) 6 else 3, if (isCurrent) Color.parseColor("#34D399") else Color.parseColor("#10B981"))
                }
                isUnlocked -> {
                    // Unlocked / Next playable level: Dark slate with cyan highlight
                    setColor(Color.parseColor("#0F172A"))
                    setStroke(6, Color.parseColor("#38BDF8"))
                }
                else -> {
                    // Locked level: Subdued dark box with subtle border
                    setColor(Color.parseColor("#070A12"))
                    setStroke(2, Color.parseColor("#1E293B"))
                }
            }
        }
        holder.container.background = bgDrawable

        when {
            isCompleted -> {
                holder.tvNum.setTextColor(Color.WHITE)
                holder.tvCheck.visibility = View.VISIBLE
                holder.tvLock.visibility = View.GONE
                holder.container.alpha = 1.0f
            }
            isUnlocked -> {
                holder.tvNum.setTextColor(Color.parseColor("#38BDF8"))
                holder.tvCheck.visibility = View.GONE
                holder.tvLock.visibility = View.GONE
                holder.container.alpha = 1.0f
            }
            else -> {
                // Locked
                holder.tvNum.setTextColor(Color.parseColor("#475569"))
                holder.tvCheck.visibility = View.GONE
                holder.tvLock.visibility = View.VISIBLE
                holder.container.alpha = 0.55f
            }
        }

        val clickListener = View.OnClickListener { v ->
            if (isUnlocked) {
                onLevelSelected(level)
            } else {
                // Tactile feedback: quick horizontal shake animation
                v.animate()
                    .translationXBy(12f)
                    .setDuration(40)
                    .withEndAction {
                        v.animate()
                            .translationXBy(-24f)
                            .setDuration(40)
                            .withEndAction {
                                v.animate()
                                    .translationX(0f)
                                    .setDuration(40)
                                    .start()
                            }.start()
                    }.start()
                android.widget.Toast.makeText(
                    v.context,
                    "🔒 Level ${level.id} is locked! Complete Level ${level.id - 1} first.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.container.setOnClickListener(clickListener)
        holder.itemView.setOnClickListener(clickListener)
    }

    override fun getItemCount(): Int = levels.size
}
