package com.service.bizstore.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.service.bizstore.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private val cartDescriptions: MutableList<String>,
    private val cartImages: MutableList<String>,
    private val cartQuantity: MutableList<Int>,
//    private val cartIngredient: MutableList<String>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var cartItemsReference: DatabaseReference
    private val itemKeys = mutableListOf<String>()  // Store Firebase keys for efficient access

    init {
        val userId = auth.currentUser?.uid ?: ""
        cartItemsReference = FirebaseDatabase.getInstance()
            .reference.child("user").child(userId).child("CartItems")

        loadItemKeys()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        if (position >= 0 && position < cartItems.size) {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            if (position >= 0 && position < cartItems.size) {
                binding.apply {
                    val quantity = cartQuantity[position]
                    cartFoodName.text = cartItems[position]
                    cartItemPrice.text = cartItemPrices[position]

                    val uri = Uri.parse(cartImages[position])
                    Glide.with(context).load(uri).into(cartImage)

                    catItemQuantity.text = quantity.toString()

                    minusbutton.setOnClickListener { decreaseQuantity(position) }
                    plusebutton.setOnClickListener { increaseQuantity(position) }
                    deleteButton.setOnClickListener { deleteItem(position) }
                }
            }
        }

        private fun increaseQuantity(position: Int) {
            if (position >= 0 && position < cartQuantity.size) {
                if (cartQuantity[position] < 10) {
                    cartQuantity[position]++
                    binding.catItemQuantity.text = cartQuantity[position].toString()

                    // Update Firebase with new quantity
                    val uniqueKey = itemKeys[position]
                    cartItemsReference.child(uniqueKey).child("quantity")
                        .setValue(cartQuantity[position])
                }
            }
        }

        private fun decreaseQuantity(position: Int) {
            if (position >= 0 && position < cartQuantity.size) {
                if (cartQuantity[position] > 1) {
                    cartQuantity[position]--
                    binding.catItemQuantity.text = cartQuantity[position].toString()

                    // Update Firebase with new quantity
                    val uniqueKey = itemKeys[position]
                    cartItemsReference.child(uniqueKey).child("quantity")
                        .setValue(cartQuantity[position])
                }
            }
        }

        private fun deleteItem(position: Int) {
            if (position >= 0 && position < cartItems.size && position < itemKeys.size) {
                val uniqueKey = itemKeys[position]

                // Remove from Firebase
                cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                    // Remove from local lists immediately
                    cartItems.removeAt(position)
                    cartImages.removeAt(position)
                    cartDescriptions.removeAt(position)
                    cartQuantity.removeAt(position)
                    cartItemPrices.removeAt(position)
//                    cartIngredient.removeAt(position)

                    // Remove key from list to prevent index mismatch
                    itemKeys.removeAt(position)

                    Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()

                    notifyItemRemoved(position)

                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    // ✅ New method to get the updated quantities of cart items
    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val updatedQuantities = mutableListOf<Int>()
        for (i in 0 until itemCount) {
            updatedQuantities.add(cartQuantity[i])
        }
        return updatedQuantities
    }

    private fun loadItemKeys() {
        cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemKeys.clear()
                for (data in snapshot.children) {
                    data.key?.let { itemKeys.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load cart items", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
