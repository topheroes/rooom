package com.example.rooom

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.room.RoomDatabase
import androidx.room.Database
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Entity
class Student{
    @PrimaryKey
    var studentName:String = ""
    var iq:String = ""
}

@Dao
interface StudentsDao{
    @Query("SELECT * FROM Student;")
    fun getAll(): LiveData<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item:Student)

}

// Song and Album are classes annotated with @Entity.
@Database(version = 1, entities = [Student::class])
abstract class MusicDatabase : RoomDatabase() {
    // SongDao is a class annotated with @Dao.
    abstract val studentDao: StudentsDao

}


class MainRecyclerView(var items:List<Student>, val context: Context?):RecyclerView.Adapter<MainRecyclerView.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_recycler_view, null)
        var holder = ViewHolder(view)
        return holder

    }

    fun changeAll(newItems:List<Student>){

        items = newItems
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = items[position].studentName
        holder.iq.text = items[position].iq

    }


    class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val name = view.findViewById<TextView>(R.id.textName)
        val iq = view.findViewById<TextView>(R.id.textIq)
    }

}


class MainViewModelFactory(val context:Context?):ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(context) as T
    }
}

class MainViewModel(val context:Context?): ViewModel(){

    val db = context?.let{Room
        .databaseBuilder(
            context,
            MusicDatabase::class.java, "database-name"
        )
        .build()    }?:null

    val all = db?.studentDao?.getAll()
}





class MainFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_main, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm = ViewModelProviders.of(this, MainViewModelFactory(context)).get(MainViewModel::class.java)
        var items:List<Student> = listOf()

        val adapter = MainRecyclerView(items, context)
        view.main_recycler.adapter = adapter
        view.main_recycler.layoutManager = LinearLayoutManager(context)

        vm.all?.observe(viewLifecycleOwner, Observer {

            adapter.changeAll(it)

        })

        button.setOnClickListener{
            val student = Student()
            student.iq = edit3.text.toString()
            student.studentName = edit1.text.toString()
            GlobalScope.launch {
                vm.db?.studentDao?.insert(student)

            }
        }

    }


}



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tr = supportFragmentManager.beginTransaction()
        tr.replace(android.R.id.content, MainFragment())
        tr.commit()

    }
}
