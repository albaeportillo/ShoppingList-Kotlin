package com.aportillo.shoppinglistkotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.aportillo.shoppinglistkotlin.adapter.itemAdapter
import com.aportillo.shoppinglistkotlin.data.AppDatabase
import com.aportillo.shoppinglistkotlin.data.Item
import com.aportillo.shoppinglistkotlin.touch.ItemRecyclerTouchCallback
import com.aportillo.shoppinglistkotlin.ui.login.LoginActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_scrolling.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class ShoppinListActivity : AppCompatActivity(), ItemDialog.ItemHandler {

    lateinit var itemAdapter: itemAdapter
    lateinit var _db: DatabaseReference
    var editIndex: Int = -1
    lateinit var userId: String

    companion object {
        val KEY_ITEM_TO_EDIT = R.string.key_item_to_edit.toString()
        var totalCost = 0F
    }

    override fun itemCreated(item: Item) {
        Thread { insertItemAndRunOnUiThread(item) }.start()
    }

    private fun insertItemAndRunOnUiThread(item: Item) {
        val itemId = AppDatabase.getInstance(
            this@ShoppinListActivity
        ).itemDao().insertItem(item)
        item.itemId = itemId
        runOnUiThread {
            itemAdapter.addItem(item)
        }

    }

    override fun itemUpdated(item: Item) {
        Thread { updateItemAndRunOnUiThread(item) }.start()
    }

    private fun updateItemAndRunOnUiThread(item: Item) {
        AppDatabase.getInstance(
            this@ShoppinListActivity
        ).itemDao().updateItem(item)
        runOnUiThread {
            itemAdapter.updateItem(item, editIndex)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        userId = intent.getStringExtra("userId")

        _db = FirebaseDatabase.getInstance().reference

        setSupportActionBar(toolbar)
        setOnClickListeners()

        if (!wasOpenedEarlier()) {
            setUpFabPrompt()
        }
        saveFirstOpenInfo()
        initRecyclerViewFromDB()


    }

    private fun setUpFabPrompt() {
        MaterialTapTargetPrompt.Builder(this)
            .setTarget(R.id.fab)
            .setPrimaryText(getText(R.string.add_item).toString())
            .setSecondaryText(getText(R.string.add_click_hint).toString())
            .show()
    }

    private fun setOnClickListeners() {
        var demoAnim = AnimationUtils.loadAnimation(
            this@ShoppinListActivity, R.anim.demo_anim
        )

        demoAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })

        fab.setOnClickListener { view ->
            fab.startAnimation(demoAnim)
            showAddItemDialog()
        }

        btnDeleteAll.setOnClickListener {
            btnDeleteAll.startAnimation(demoAnim)
            Thread {
                AppDatabase.getInstance(this@ShoppinListActivity).itemDao().deleteAll()
                runOnUiThread {


                    for (_item in itemAdapter.items) {
                        _db.child("item").child(_item.random).removeValue()
                    }
                    itemAdapter.removeAll()
                    updateTotalCost()
                }
            }.start()
        }
        logout.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this@ShoppinListActivity, LoginActivity::class.java))
        }
        share.setOnClickListener {
            var listItem : ArrayList<String> = ArrayList()
            listItem.add(getString(R.string.app_name)+" "+ userId +": "+ "\n")
            var i : Int = 0
            for (_item in itemAdapter.items) {
                i = i.inc()
                var itemShare : String
                itemShare = i.toString() +"- "+ getString(R.string.item_category)+": "+ _item.category +", "+getString(R.string.item_name_holder)+": " +
                        ""+_item.name+", "+getString(R.string.item_description_holder)+": "+ _item.description+", "+getString(R.string.item_price_holder)+": " +
                        ""+ _item.price +", "+getString(R.string.item_quantity_holder)+": "+ _item.quantity+", "+getString(R.string.purchased)+":" +
                        " "+ if (_item.status==true) "Si. " else "No. " + "\n"
                listItem.add(itemShare)
            }

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, listItem.toString())
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getString(R.string.share_chooser_title)))
        }



    }


    fun saveFirstOpenInfo() {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var editor = sharedPref.edit()
        editor.putBoolean(getText(R.string.key_open).toString(), true)
        editor.apply()
    }

    fun wasOpenedEarlier(): Boolean {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPref.getBoolean(getText(R.string.key_open).toString(), false)
    }

    private fun initRecyclerViewFromDB() {
        Thread { getItemsAndRunOnUiThread() }.start()
    }

    private fun getItemsAndRunOnUiThread() {
        var listItems =
            AppDatabase.getInstance(this@ShoppinListActivity).itemDao().getAllItems(EncodeString(userId).toString())

        runOnUiThread {
            itemAdapter = itemAdapter(this, listItems, tvTotalCost, _db)
            recyclerItem.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this)
            recyclerItem.adapter = itemAdapter

            val itemDecoration =
                androidx.recyclerview.widget.DividerItemDecoration(
                    this,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            recyclerItem.addItemDecoration(itemDecoration)

            val callback = ItemRecyclerTouchCallback(itemAdapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerItem)

            updateTotalCost()
        }
    }

    private fun showAddItemDialog() {
        val createItemDialog = ItemDialog()
        val bundle = Bundle()
        bundle.putString("userId", userId)
        createItemDialog.arguments = bundle
        createItemDialog.show(supportFragmentManager, getText(R.string.tag_item).toString())
    }

    fun showEditItemDialog(itemToEdit: Item, idx: Int) {
        editIndex = idx
        val editItemDialog = ItemDialog()
        val bundle = Bundle()
        bundle.putString("userId", userId)
        bundle.putSerializable(KEY_ITEM_TO_EDIT, itemToEdit)
        editItemDialog.arguments = bundle
        editItemDialog.show(supportFragmentManager, R.string.edit_item_dialog.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun EncodeString(string: String): String? {
        return string.replace(".", ",")
    }

    fun updateTotalCost() {
        for (_item in itemAdapter.items) {
            totalCost = totalCost +_item.price * _item.quantity.toFloat()
        }

        var context : Context =this@ShoppinListActivity

        tvTotalCost.text =
            context.resources.getString(
                R.string.total_cost,
                context.resources.getString(R.string.format_string).format(ShoppinListActivity.totalCost)
            )

    }

}