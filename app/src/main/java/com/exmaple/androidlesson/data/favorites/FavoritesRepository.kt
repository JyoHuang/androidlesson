package com.exmaple.androidlesson.data.favorites

import com.exmaple.androidlesson.presentation.search.StockQuote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class FavoriteStock(
    val code: String = "",
    val name: String = "",
    val lastPrice: Double? = null,
    val createdAt: Long? = null
)

object FavoritesRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun userFavoritesCollection() =
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("favorites")
        }

    /**
     * 加入 / 更新我的最愛
     * 用股票代號當作 documentId，避免重複
     */
    fun addFavorite(
        quote: StockQuote,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(false, "尚未登入，無法加入我的最愛。")
            return
        }

        val col = userFavoritesCollection()
        if (col == null) {
            onResult(false, "無法取得收藏路徑。")
            return
        }

        val data = mapOf(
            "code" to quote.code,
            "name" to quote.name,
            "lastPrice" to quote.lastPrice,
            "createdAt" to System.currentTimeMillis()
        )

        col.document(quote.code)
            .set(data)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    /**
     * 監聽我的最愛清單變化（即時更新）
     */
    fun observeFavorites(
        onResult: (List<FavoriteStock>, String?) -> Unit
    ): ListenerRegistration? {
        val col = userFavoritesCollection()
        if (col == null) {
            onResult(emptyList(), "尚未登入，無法讀取收藏。")
            return null
        }

        return col
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onResult(emptyList(), e.message)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onResult(emptyList(), null)
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map { doc ->
                    FavoriteStock(
                        code = doc.getString("code") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        lastPrice = doc.getDouble("lastPrice"),
                        createdAt = doc.getLong("createdAt")
                    )
                }
                onResult(list, null)
            }
    }

    /**
     * 刪除我的最愛
     */
    fun deleteFavorite(
        code: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val col = userFavoritesCollection()
        if (col == null) {
            onResult(false, "尚未登入，無法刪除收藏。")
            return
        }

        col.document(code)
            .delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }
    /**
     * 監聽某股票是否已加入我的最愛（即時更新）
     */
    fun observeIsFavorite(
        code: String,
        onResult: (Boolean) -> Unit
    ): ListenerRegistration? {
        val col = userFavoritesCollection() ?: return null

        // ⭐ 重點：code 是空的時候，直接當「未收藏」，不要去監聽
        if (code.isBlank()) {
            onResult(false)
            return null
        }

        return col.document(code)
            .addSnapshotListener { doc, _ ->
                onResult(doc?.exists() == true)
            }
    }

}
