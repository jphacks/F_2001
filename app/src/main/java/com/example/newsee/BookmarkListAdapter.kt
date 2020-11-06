package com.example.newsee

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.realm.RealmResults


@RequiresApi(Build.VERSION_CODES.R)
class BookmarkListAdapter(context: Context, resource: Int, private val results: RealmResults<Bookmark>, private val moveBrowser: ((link: String) -> Unit)?) :
    ArrayAdapter<Bookmark>(context, resource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: createView(parent)
        val holder = view.tag as ItemViewHolder

        results[position]?.let { bookmark ->
            holder.titleText.text = bookmark.title
            holder.descriptionText.text = bookmark.description
            holder.linkButton.setOnClickListener {
                moveBrowser?.invoke(bookmark.link)
            }
            holder.bookmarkButton
            holder.bookmarkButton.setOnClickListener {
                // ブックマークリストから記事を削除
                BookmarksService.delete(bookmark.link)
            }
        }

        return view
    }

    override fun getCount() = results.size

    inner class ItemViewHolder(itemView: View) {
        val titleText: TextView
        val descriptionText: TextView
        val linkButton: ImageButton
        val bookmarkButton: ImageButton

        init {
            itemView.apply {
                titleText = findViewById(R.id.feed_title)
                descriptionText = findViewById(R.id.feed_description)
                linkButton = findViewById(R.id.detail_button)
                bookmarkButton = findViewById(R.id.bookmark_button)
            }
        }
    }

    private fun createView(parent: ViewGroup) : View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bookmark_list_item, parent, false)
        view.tag = ItemViewHolder(view)

        return view
    }
}
