package com.rickyslash.deepnavigationapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rickyslash.deepnavigationapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpenDetail.setOnClickListener(this)

        showNotification(this@MainActivity, getString(R.string.notification_title), getString(R.string.notification_message), 110)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_open_detail) {
            val detailIntent = Intent(this@MainActivity, DetailActivity::class.java)
            detailIntent.putExtra(DetailActivity.EXTRA_TITLE, getString(R.string.detail_title))
            detailIntent.putExtra(DetailActivity.EXTRA_MESSAGE, getString(R.string.detail_message))
            startActivity(detailIntent)
        }
    }

    private fun showNotification(context: Context, title: String, message: String, notifId: Int) {
        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Navigation Channel"

        // make intent that will be passed for `addNextIntent()`
        val notifDetailIntent = Intent(this, DetailActivity::class.java)
        notifDetailIntent.putExtra(DetailActivity.EXTRA_TITLE, title)
        notifDetailIntent.putExtra(DetailActivity.EXTRA_MESSAGE, message)

        // this will make a stack with parent of `DetailActivity` (means, it also contains MainActivity at the back)
        val pendingIntent = TaskStackBuilder.create(this).run {

            addParentStack(DetailActivity::class.java)
            addNextIntent(notifDetailIntent)

            // this ensure if matching intent already exist (same activity)
            // if it match, it will update the data from `addParentStac()` with `addNextIntent`
            // this also returns `PendingIntent`
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(110, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(110, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        // get Notification System Service
        val notificationManagerCompat = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // get uri for user's notification ringtone
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // construct the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_email_24)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.white))
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setSound(alarmSound)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // this is the additional code for android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000)

            builder.setChannelId(CHANNEL_ID)

            notificationManagerCompat.createNotificationChannel(channel)
        }

        // build the notification that's already been constructed
        val notification = builder.build()

        // show the notification
        notificationManagerCompat.notify(notifId, notification)
    }
}

// when an 'app is just barely opened', it 'doesn't have task'
// 'app will make a new task' by 'searching activity' that has '<intent-filter>' with 'Main' <action> as category LAUNCHER
// that 'MainActivity' will be inserted to 'back stack' inside 'foreground/running' as 'root activity'

// when 'activity is changed', 'MainActivity' will be stopped
// all of its 'state will be saved automatically' to 'Bundle object'
// then 'onStop()' callback from 'MainActivity' will be called

// say activity is changed to 'DetailActivity'
// 'DetailActivity' is now on 'foreground/running' and is at the top of 'back stack'

// when user clicked 'device Back button', 'DetailActivity' is going to be the first being out (LIFO)
// 'onDestroy()' from 'DetailActivity' will be called and state will be deleted from memory
// 'MainActivity' will be back to 'foreground/running'

// This process is called 'Back Stack' & its ' Task'
// - Task is a group of activity that is doing some action

// An android app could have numerous task
// But only 1 task that can be run on 'foreground/running'
// the rest could be put on background by multitasking

// Launch Mode & Flag: to modify the behavior of 'Back Stack' in certain condition (like on SearchView Activity)
// - Standard (default): when intent, new instance will be made. There could be more than 1 intent in a 'back stack'
// - singleTop | FLAG_ACTIVITY_SINGLE_TOP:  use same instance when it's on the top of the task, then call onNewIntent()
// - singleTask | FLAG_ACTIVITY_NEW_TASK: use same instance inside the 'back stack' anywhere it's position, following for the instance on its bottom, then call onNewIntent
// - singleInstance: only 1 instance inside a task

// TaskStackBuilder: To make sure the 'Back Stack' is still working even when you have shortcut to a deep positioned activity
// - example: when clicking 'compose' from 'gmail widget', it will open 'composeActivity'
// --- On the 'composeActivity' , after clicking Back, it should be redirected to 'MainActivity', 'not' to 'Home Screen'
// - <android:parentActivityName> need to be written in targeted <activity> inside androidManifest

// 'Up Button' is the 'back button' in 'AppBar'
// 'Back Button' is the 'back button' in 'device'