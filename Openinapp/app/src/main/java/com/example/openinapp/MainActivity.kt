package com.example.openinapp

// Main activity (Kotlin)

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {

    // Copy link purpose
    private lateinit var clipboardManager: ClipboardManager

    // Line chart purpose
    private lateinit var lineGraphView: GraphView
    private lateinit var series: LineGraphSeries<DataPoint>

    // Rectangular cards for top links and recent links
    private lateinit var containerLayout: LinearLayout

    private lateinit var cardView: LinearLayout
    private lateinit var title: TextView
    private lateinit var date: TextView
    private lateinit var number: TextView
    private lateinit var link: TextView
    private lateinit var image: ImageView

    private lateinit var topLinksButton: Button
    private lateinit var recentLinksButton: Button

    // Analytics represented in square cards
    private lateinit var todayClicks: LinearLayout
    private lateinit var result1: TextView
    private lateinit var logo1: ImageView
    private lateinit var cat1: TextView

    private lateinit var topLocation: LinearLayout
    private lateinit var result2: TextView
    private lateinit var cat2: TextView
    private lateinit var logo2: ImageView

    private lateinit var topSource: LinearLayout
    private lateinit var logo3: ImageView
    private lateinit var result3: TextView
    private lateinit var cat3: TextView

    private lateinit var bestTime: LinearLayout
    private lateinit var logo4: ImageView
    private lateinit var result4: TextView
    private lateinit var cat4: TextView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        lineGraphView = findViewById(R.id.idGraphView)
        series = LineGraphSeries()
       // lineGraphView.addSeries(series)

        // First UseCase :
        // 1. Display greeting from the local time

        val greetingTextView: TextView = findViewById(R.id.greeting)

        val calendar = Calendar.getInstance()
        val currentTime = calendar.time

        val dateFormat = SimpleDateFormat("HH", Locale.getDefault())
        val hour = dateFormat.format(currentTime).toInt()

        val greeting = when (hour) {
            in 0..11 -> "Welcome, Rise and shine!"
            in 12..16 -> "Good Afternoon!"
            else -> "Hello,how's your evening?"
        }

        // Setting the greeting in the TextView
        greetingTextView.text = greeting

        //----------------------------------


        todayClicks = findViewById(R.id.top_links)
        logo1 = todayClicks.findViewById(R.id.logo)
        result1 = todayClicks.findViewById(R.id.result)
        cat1 = todayClicks.findViewById(R.id.category)

        topLocation = findViewById(R.id.top_location)
        logo2 = topLocation.findViewById(R.id.logo)
        result2 = topLocation.findViewById(R.id.result)
        cat2 = topLocation.findViewById(R.id.category)

        topSource = findViewById(R.id.top_source)
        logo3 = topSource.findViewById(R.id.logo)
        result3 = topSource.findViewById(R.id.result)
        cat3 = topSource.findViewById(R.id.category)

        bestTime = findViewById(R.id.best_time)
        logo4 = bestTime.findViewById(R.id.logo)
        result4 = bestTime.findViewById(R.id.result)
        cat4 = bestTime.findViewById(R.id.category)

        containerLayout = findViewById(R.id.containerLayout)


        // Coroutine-based API Call with Exception Handling and UI Thread Interaction
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch(Dispatchers.IO) {
            try {
                val response = apiCall()
                // Processing the successful response on the UI thread
                runOnUiThread {
                    processApiResponse(response)
                }
            } catch (e: Exception) {
                // Handling the exception on the UI thread
                runOnUiThread {
                    handleApiFailure(e)
                }
            }
        }
    }
    // End of OnCreate

    // suspend function for api request and receiving the response
    private suspend fun apiCall(): JSONObject {
        val client = OkHttpClient()

        val apiUrl = "https://api.inopenapp.com/api/v1/dashboardNew"

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader(
                "Authorization",
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MjU5MjcsImlhdCI6MTY3NDU1MDQ1MH0.dCkW0ox8tbjJA2GgUx2UEwNlbTZ7Rr38PVFJevYcXFI"
            )
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            throw IOException("Unexpected response code: ${response.code}")
        }

        val responseData = response.body?.string()
        return JSONObject(responseData)
    }

    // Function to handle and display the api response
    private fun processApiResponse(jsonObject: JSONObject) {
        result1.text = jsonObject.getInt("today_clicks").toString()
        cat1.text = "Today's Clicks"
        logo1.setImageResource(R.drawable.clicks)

        result2.text = jsonObject.getString("top_location")
        cat2.text = "Top Location"
        logo2.setImageResource(R.drawable.location)

        result3.text = jsonObject.getString("top_source")
        cat3.text = "Top Source"
        logo3.setImageResource(R.drawable.source)

        val dataObject = jsonObject.getJSONObject("data")

        // Third UseCase
        // 3. Create a chart from the given api response
        val data = dataObject.getJSONObject("overall_url_chart")

        val keys = data.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val parts =key.split("-")
            val date = parts[1].toDouble() + parts[2].toDouble()/100
            val count = data.getInt(key) //count
            val clicks = count.toDouble()

            series.appendData(DataPoint(date, clicks),true,10)
        }
        // on below line adding animation
        lineGraphView.animate()

        // on below line we are setting scrollable for point graph view
        lineGraphView.viewport.isScrollable = true

        // on below line we are setting scalable.
        lineGraphView.viewport.isScalable = true

        // on below line we are setting scalable y
        lineGraphView.viewport.setScalableY(true)

        // on below line we are setting scrollable y
        lineGraphView.viewport.setScrollableY(true)

        // on below line we are setting color for series.
        R.color.blue.also { series.color = it }

        // on below line we are adding data series to our graph view.
        lineGraphView.addSeries(series)

        //-------------------------------------------

        // Handling the TopLinks and RecentLinks from the api response
        topLinksButton = findViewById(R.id.topLinksTab)
        recentLinksButton = findViewById(R.id.recentLinksTab)

        // Fourth UseCase
        // 4. Add a Tab [Top links & Recent links] and create a list view to display the data you shall be getting from the api response
        // Accessing TopLinks
        topLinksButton.setOnClickListener {
            containerLayout.removeAllViews()
            topLinksButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
            recentLinksButton.setBackgroundColor(ContextCompat.getColor(this, R.color.initial))

            topLinksButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            recentLinksButton.setTextColor(ContextCompat.getColor(this, R.color.lightBlack))

            val topLinksArray = dataObject.getJSONArray("top_links")
            for (i in 0 until topLinksArray.length()) {
                val topLinkObject = topLinksArray.getJSONObject(i)

                val webLink = topLinkObject.getString("web_link")
                val title = topLinkObject.getString("title")
                val totalClicks = topLinkObject.getInt("total_clicks")
                val originalImage = topLinkObject.getString("original_image")
                val createdAt = topLinkObject.getString("created_at")
                addCardToContainer(webLink , totalClicks, title, originalImage, createdAt )

            }
        }

        // Accessing RecentLinks
        recentLinksButton.setOnClickListener {
            containerLayout.removeAllViews()
            recentLinksButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
            topLinksButton.setBackgroundColor(ContextCompat.getColor(this, R.color.initial))

            recentLinksButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            topLinksButton.setTextColor(ContextCompat.getColor(this, R.color.lightBlack))

            val recentLinksArray = dataObject.getJSONArray("recent_links")
            for (i in 0 until recentLinksArray.length()) {
                val recentLinkObject = recentLinksArray.getJSONObject(i)

                val webLink = recentLinkObject.getString("web_link")
                val title = recentLinkObject.getString("title")
                val totalClicks = recentLinkObject.getInt("total_clicks")
                val originalImage = recentLinkObject.getString("original_image")
                val createdAt = recentLinkObject.getString("created_at")
                addCardToContainer(webLink , totalClicks, title, originalImage, createdAt )

            }
        }

    }

    // Function to display the links as list view
    private fun addCardToContainer(webLink: String, totalClicks: Int, title1: String, originalImage: String, created: String) {
        cardView = layoutInflater.inflate(R.layout.rectangular_card, null) as LinearLayout

        title = cardView.findViewById(R.id.titleOfSource)
        title.text=title1

        date = cardView.findViewById(R.id.date)
        date.text = created.substring(0,10)

        number = cardView.findViewById(R.id.clicks)
        number.text = totalClicks.toString()

        link = cardView.findViewById(R.id.link)
        link.text = webLink

        image = cardView.findViewById(R.id.image)
        Picasso.get().load(originalImage).into(image)

        // To allow the user to Copy link
        link.setOnClickListener{
            val clipData = ClipData.newPlainText("Text", link.text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Set the margin bottom to create space between cards
        layoutParams.setMargins(0, 0, 0, 80)
        cardView.layoutParams = layoutParams

        containerLayout.addView(cardView)
    }

    // Function to handle api failure
    private fun handleApiFailure(e: Exception) {
        Toast.makeText(
            applicationContext,
            "Request failed: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
        e.printStackTrace()
    }
}
//-------------------------------------------------