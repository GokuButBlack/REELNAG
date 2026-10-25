package com.example.domain

import com.example.data.NagPersonality
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class NagMessageTemplate(
    val id: String,
    val personality: NagPersonality,
    val template: String
)

object SmartNagMessageEngine {

    private val templates: Map<NagPersonality, List<NagMessageTemplate>> = mapOf(
        NagPersonality.SARCASTIC_BESTIE to listOf(
            NagMessageTemplate("sb_1", NagPersonality.SARCASTIC_BESTIE, "Bro, you saved \"{title}\" {days} ago. Are you collecting links as a hobby?"),
            NagMessageTemplate("sb_2", NagPersonality.SARCASTIC_BESTIE, "You have {unwatched} unwatched Reels rotting in your locker, starting with \"{title}\". Museum curator behavior."),
            NagMessageTemplate("sb_3", NagPersonality.SARCASTIC_BESTIE, "Another day, another excuse not to watch \"{title}\" in {category}. It's literally 30 seconds long."),
            NagMessageTemplate("sb_4", NagPersonality.SARCASTIC_BESTIE, "If saving \"{title}\" burned calories, you'd be an Olympian. Actually watching it? Still at zero."),
            NagMessageTemplate("sb_5", NagPersonality.SARCASTIC_BESTIE, "You snoozed \"{title}\" {snoozes} times. At this point just admit you're scared of commitment."),
            NagMessageTemplate("sb_6", NagPersonality.SARCASTIC_BESTIE, "\"{title}\" has been sitting in {category} for {days}. Even milk expires slower than your attention span."),
            NagMessageTemplate("sb_7", NagPersonality.SARCASTIC_BESTIE, "Quick check-in: did you save \"{title}\" to watch it, or are you building a digital hoard for future archaeologists?"),
            NagMessageTemplate("sb_8", NagPersonality.SARCASTIC_BESTIE, "You plus {unwatched} unwatched Reels equals peak delusion. Go watch \"{title}\" right now, bestie."),
            NagMessageTemplate("sb_9", NagPersonality.SARCASTIC_BESTIE, "Oh look, it's \"{title}\" waving from the bottom of your {category} pile. Give it 45 seconds of your life.")
        ),
        NagPersonality.CHILL_FRIEND to listOf(
            NagMessageTemplate("cf_1", NagPersonality.CHILL_FRIEND, "That Reel \"{title}\" you saved is still waiting whenever you have a minute."),
            NagMessageTemplate("cf_2", NagPersonality.CHILL_FRIEND, "No rush at all, just a friendly heads-up that \"{title}\" in {category} is ready when you want a break."),
            NagMessageTemplate("cf_3", NagPersonality.CHILL_FRIEND, "Hey! You saved \"{title}\" {days} ago. Grab a coffee and give it a quick watch if you're free."),
            NagMessageTemplate("cf_4", NagPersonality.CHILL_FRIEND, "You've got {unwatched} saved Reels tucked away. \"{title}\" looks like a cozy one to start with."),
            NagMessageTemplate("cf_5", NagPersonality.CHILL_FRIEND, "Whenever your brain needs a 60-second breather today, \"{title}\" is waiting in your {category} folder."),
            NagMessageTemplate("cf_6", NagPersonality.CHILL_FRIEND, "Zero pressure—you snoozed \"{title}\" earlier, so I'm just floating it back to the top for you."),
            NagMessageTemplate("cf_7", NagPersonality.CHILL_FRIEND, "Past-you thought \"{title}\" was worth saving {days} ago. Present-you might enjoy it right now."),
            NagMessageTemplate("cf_8", NagPersonality.CHILL_FRIEND, "Just keeping your {category} list tidy: \"{title}\" is still unwatched whenever the vibe feels right."),
            NagMessageTemplate("cf_9", NagPersonality.CHILL_FRIEND, "Gentle nudge: \"{title}\" is queued up in your locker. Watch it now or snooze it—totally your call.")
        ),
        NagPersonality.DRAMA_QUEEN to listOf(
            NagMessageTemplate("dq_1", NagPersonality.DRAMA_QUEEN, "Breaking news: a legendary Reel (\"{title}\") remains unwatched for {days}! The tragedy!"),
            NagMessageTemplate("dq_2", NagPersonality.DRAMA_QUEEN, "GASP! \"{title}\" has been abandoned in the cold dark depths of {category} with {unwatched} other neglected masterpieces!"),
            NagMessageTemplate("dq_3", NagPersonality.DRAMA_QUEEN, "Sound the alarms! You promised yourself you'd watch \"{title}\" and yet the cinema stays dark!"),
            NagMessageTemplate("dq_4", NagPersonality.DRAMA_QUEEN, "I cannot—I simply CANNOT—watch \"{title}\" get snoozed {snoozes} times in a row! Have you no heart?!"),
            NagMessageTemplate("dq_5", NagPersonality.DRAMA_QUEEN, "Stop the presses! It has been {days} since \"{title}\" entered your life and you still haven't pressed play!"),
            NagMessageTemplate("dq_6", NagPersonality.DRAMA_QUEEN, "The tension is unbearable! {unwatched} Reels are crying out for attention, led by \"{title}\"!"),
            NagMessageTemplate("dq_7", NagPersonality.DRAMA_QUEEN, "An emergency broadcast from your {category} locker: \"{title}\" demands its red-carpet premiere immediately!"),
            NagMessageTemplate("dq_8", NagPersonality.DRAMA_QUEEN, "Betrayal! Heartbreak! Cinema left on read! Watch \"{title}\" before I faint dramatically on this sofa!"),
            NagMessageTemplate("dq_9", NagPersonality.DRAMA_QUEEN, "History will remember where you were when you finally watched \"{title}\" after {days} of suspense!")
        ),
        NagPersonality.MOTIVATOR to listOf(
            NagMessageTemplate("mo_1", NagPersonality.MOTIVATOR, "Your next great idea might be hiding in that saved Reel: \"{title}\"."),
            NagMessageTemplate("mo_2", NagPersonality.MOTIVATOR, "Creators don't just hoard inspiration—they execute! Open \"{title}\" in {category} and take one actionable note."),
            NagMessageTemplate("mo_3", NagPersonality.MOTIVATOR, "You saved \"{title}\" {days} ago for a reason. Turn that spark into momentum today!"),
            NagMessageTemplate("mo_4", NagPersonality.MOTIVATOR, "Knock out 1 of your {unwatched} unwatched Reels right now! Start with \"{title}\" and level up your craft."),
            NagMessageTemplate("mo_5", NagPersonality.MOTIVATOR, "Small habits build big results: spend 60 seconds reviewing \"{title}\" and clear your {category} queue!"),
            NagMessageTemplate("mo_6", NagPersonality.MOTIVATOR, "Even after {snoozes} snoozes, today is a fresh start. Press play on \"{title}\" and get inspired!"),
            NagMessageTemplate("mo_7", NagPersonality.MOTIVATOR, "Future-you will thank present-you for finally unlocking the gold inside \"{title}\". Let's go!"),
            NagMessageTemplate("mo_8", NagPersonality.MOTIVATOR, "Momentum loves action! Clear \"{title}\" off your {category} list after {days} and keep winning."),
            NagMessageTemplate("mo_9", NagPersonality.MOTIVATOR, "Every top creator studies their saved vault. Review \"{title}\" now and turn it into your next post!")
        ),
        NagPersonality.UNHINGED_FRIEND to listOf(
            NagMessageTemplate("uf_1", NagPersonality.UNHINGED_FRIEND, "Your saved folder has more commitment issues than your entire friend group. Watch \"{title}\" now."),
            NagMessageTemplate("uf_2", NagPersonality.UNHINGED_FRIEND, "I am living inside your phone walls until you watch \"{title}\". It's been {days}, bestie. Release me."),
            NagMessageTemplate("uf_3", NagPersonality.UNHINGED_FRIEND, "{unwatched} unwatched Reels?! The digital raccoons in your {category} folder have unionized around \"{title}\"."),
            NagMessageTemplate("uf_4", NagPersonality.UNHINGED_FRIEND, "Snoozing \"{title}\" {snoozes} times is crazy work. The algorithm is personally side-eyeing you."),
            NagMessageTemplate("uf_5", NagPersonality.UNHINGED_FRIEND, "Blink twice if you forgot \"{title}\" even existed. You saved it {days} ago in a 2 AM fever dream."),
            NagMessageTemplate("uf_6", NagPersonality.UNHINGED_FRIEND, "Your {category} locker just called a family meeting about \"{title}\". Watch it before things get weird."),
            NagMessageTemplate("uf_7", NagPersonality.UNHINGED_FRIEND, "Save button: abused. Play button: untouched. \"{title}\": crying in 4K. Fix this immediately."),
            NagMessageTemplate("uf_8", NagPersonality.UNHINGED_FRIEND, "If you save one more Reel before watching \"{title}\" ({unwatched} waiting!), I'm replacing your alarm with kazoo music."),
            NagMessageTemplate("uf_9", NagPersonality.UNHINGED_FRIEND, "30 seconds to watch \"{title}\" vs {days} of ignoring it. Your procrastination stats are hall-of-fame tier.")
        )
    )

    private val dailyGreetings: Map<NagPersonality, List<String>> = mapOf(
        NagPersonality.SARCASTIC_BESTIE to listOf(
            "Welcome back to your digital museum of Reels you swore you'd watch five minutes later.",
            "Oh look who remembered this app exists! Ready to actually watch a Reel today or just hoarding more?",
            "Your Save thumb is strong, let's see if your Play thumb still works today.",
            "Another glorious day to pretend your 'Learn Later' folder isn't collecting cobwebs."
        ),
        NagPersonality.CHILL_FRIEND to listOf(
            "Hey there! Take your time—your saved Reels and creative ideas are right where you left them.",
            "Good to see you! Whether you watch one Reel or brainstorm a new post, no rush today.",
            "Everything's organized and waiting for whenever you have a spare two minutes.",
            "Welcome in! Grab a drink and browse your saved inspiration at your own pace."
        ),
        NagPersonality.DRAMA_QUEEN to listOf(
            "Thank heavens you've returned! Your saved Reels were on the verge of a full theatrical meltdown!",
            "The spotlight is on! Will today be the day your unwatched cinema finally sees the light?!",
            "Darlings, the creative tension in your Reel Locker is palpable today!",
            "You're here! Roll out the velvet carpet—your saved inspirations await their grand premiere!"
        ),
        NagPersonality.MOTIVATOR to listOf(
            "Let's turn saved inspiration into real creative output today. You've got this!",
            "Every saved Reel is a seed for your next breakthrough idea. Let's make progress!",
            "Consistency beats perfection—watch one saved Reel and jot down one fresh hook today!",
            "Ready to build? Your locker is packed with raw material for your next big video."
        ),
        NagPersonality.UNHINGED_FRIEND to listOf(
            "Your saved Reels formed a support group while you were gone. Go say hi before they revolt.",
            "Brain cells: optional. Saved links: overflowing. Let's clear out the chaos today!",
            "I shook your Reel Locker and three tutorials from last week fell out. Time to lock in.",
            "No thoughts, just 47 tabs of 'I'll totally try this recipe'. Let's actually open one!"
        )
    )

    fun generateNagMessage(
        personality: NagPersonality,
        itemTitle: String,
        savedAtMillis: Long,
        unwatchedCount: Int,
        snoozeCount: Int = 0,
        categoryName: String = "Favorites",
        lastTemplateId: String? = null
    ): Pair<String, String> {
        val pool = templates[personality] ?: templates.values.first()
        val candidates = if (pool.size > 1 && lastTemplateId != null) {
            pool.filter { it.id != lastTemplateId }
        } else {
            pool
        }

        // Prefer snooze-specific template if snoozeCount > 0 and not just used
        val chosen = if (snoozeCount > 0) {
            candidates.firstOrNull { it.template.contains("{snoozes}") } ?: candidates.random()
        } else {
            candidates.random()
        }

        val daysDiff = max(
            1L,
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - savedAtMillis)
        )
        val daysText = if (daysDiff == 1L) "1 day" else "$daysDiff days"
        val cleanTitle = itemTitle.ifBlank { "that Reel you saved" }
        val effectiveUnwatched = max(1, unwatchedCount)

        val formatted = chosen.template
            .replace("{title}", cleanTitle)
            .replace("{days}", daysText)
            .replace("{unwatched}", effectiveUnwatched.toString())
            .replace("{snoozes}", max(1, snoozeCount).toString())
            .replace("{category}", categoryName.ifBlank { "Saved" })

        return chosen.id to formatted
    }

    fun getDailyGreeting(personality: NagPersonality, previousIndex: Int): Pair<Int, String> {
        val list = dailyGreetings[personality] ?: dailyGreetings.values.first()
        val availableIndices = list.indices.filter { it != previousIndex }.ifEmpty { list.indices.toList() }
        val nextIndex = availableIndices.random()
        return nextIndex to list[nextIndex]
    }

    /**
     * Checks whether a given hour/minute falls inside user-configured quiet hours (e.g. 22:30 to 08:00).
     */
    fun isWithinQuietHours(
        quietHoursEnabled: Boolean,
        startStr: String,
        endStr: String,
        currentHour: Int,
        currentMinute: Int
    ): Boolean {
        if (!quietHoursEnabled) return false
        val startMin = parseMinutesSinceMidnight(startStr) ?: return false
        val endMin = parseMinutesSinceMidnight(endStr) ?: return false
        val nowMin = currentHour * 60 + currentMinute

        return if (startMin <= endMin) {
            nowMin in startMin..endMin
        } else {
            // Spans midnight (e.g. 22:30 -> 08:00)
            nowMin >= startMin || nowMin <= endMin
        }
    }

    private fun parseMinutesSinceMidnight(hhmm: String): Int? {
        val parts = hhmm.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }
}
