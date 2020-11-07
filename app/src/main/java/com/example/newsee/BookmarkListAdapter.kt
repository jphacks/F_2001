package com.example.newsee

import android.content.Context
import android.os.Build
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.realm.RealmResults
import java.lang.Integer.max
import kotlin.math.min


@RequiresApi(Build.VERSION_CODES.R)
class BookmarkListAdapter(context: Context, private val resource: Int, private val results: RealmResults<Bookmark>, private val moveBrowser: ((link: String) -> Unit)?) :
    ArrayAdapter<Bookmark>(context, resource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (results.size == 0) {
            return LayoutInflater.from(parent.context).inflate(R.layout.bookmark_list_item_empty, parent, false)
        }

        val view = if (convertView?.id == R.layout.bookmark_list_item) convertView else createView(parent)
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
                FeedsService.unBookmark(bookmark.link)
            }
        }

        return view
    }

    override fun getCount() = max(results.size, 1)

    inner class ItemViewHolder(itemView: View) {
        val titleText: TextView
        val descriptionText: TextView
        val linkButton: ImageButton
        val bookmarkButton: ImageButton

        init {
            itemView.apply {
                titleText = findViewById(R.id.bookmark_title)
                descriptionText = findViewById(R.id.feed_description)
                linkButton = findViewById(R.id.detail_button)
                bookmarkButton = findViewById(R.id.bookmark_button)
            }
        }
    }

    private fun createView(parent: ViewGroup) : View {
        val view = LayoutInflater.from(parent.context).inflate(resource, parent, false)
        view.tag = ItemViewHolder(view)

        return view
    }
}
