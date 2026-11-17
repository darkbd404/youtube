package com.asmaster.adblocker

private val T: Map<String, Pair<String, String>> = mapOf(
    "home" to ("Home" to "হোম"),
    "stats" to ("Statistics" to "পরিসংখ্যান"),
    "dns" to ("DNS Protection" to "ডিএনএস সুরক্ষা"),
    "settings" to ("Settings" to "সেটিংস"),
    "prot_on" to ("Protection active" to "সুরক্ষা চালু"),
    "prot_off" to ("Protection off" to "সুরক্ষা বন্ধ"),
    "prot_need" to ("Enable the accessibility service to start" to "শুরু করতে অ্যাক্সেসিবিলিটি সার্ভিস চালু করুন"),
    "auto" to ("Auto Skip" to "অটো স্কিপ"),
    "auto_on" to ("Visible Skip Ads buttons are clicked automatically" to "Skip Ads বাটন দেখা গেলে নিজে ক্লিক হবে"),
    "auto_off" to ("Automatic detection is stopped" to "অটোমেটিক ডিটেকশন বন্ধ"),
    "yt_ok" to ("YouTube is installed" to "YouTube ইনস্টল করা আছে"),
    "yt_no" to ("YouTube is not installed" to "YouTube ইনস্টল নেই"),
    "open_yt" to ("Open YouTube" to "YouTube খুলুন"),
    "today" to ("Today" to "আজ"),
    "yesterday" to ("Yesterday" to "গতকাল"),
    "week" to ("Last 7 days" to "গত ৭ দিন"),
    "month" to ("Last 30 days" to "গত ৩০ দিন"),
    "total" to ("Total" to "মোট"),
    "a11y_on" to ("Accessibility service: ON" to "অ্যাক্সেসিবিলিটি সার্ভিস: চালু"),
    "a11y_off" to ("Accessibility service: OFF" to "অ্যাক্সেসিবিলিটি সার্ভিস: বন্ধ"),
    "a11y_btn" to ("Open Accessibility settings" to "অ্যাক্সেসিবিলিটি সেটিংস খুলুন"),
    "a11y_why" to (
        "AS MASTER reads only the YouTube screen to find a visible Skip Ads button and taps it. It does not collect, store or send any data. Ads without a Skip button cannot be skipped." to
        "AS MASTER শুধু YouTube স্ক্রিনে দৃশ্যমান Skip Ads বাটন খুঁজে ট্যাপ করে। কোনো তথ্য সংগ্রহ, সংরক্ষণ বা পাঠানো হয় না। যে বিজ্ঞাপনে Skip বাটন থাকে না সেটি স্কিপ করা যায় না।"),
    "chart" to ("Last 7 days" to "গত ৭ দিনের চার্ট"),
    "history" to ("History" to "ইতিহাস"),
    "no_history" to ("No skips recorded yet" to "এখনো কোনো স্কিপ রেকর্ড নেই"),
    "copy" to ("Copy" to "কপি"),
    "copied" to ("Copied" to "কপি হয়েছে"),
    "select" to ("Select" to "বেছে নিন"),
    "selected" to ("Selected" to "নির্বাচিত"),
    "custom_host" to ("Custom hostname (e.g. abc123.dns.nextdns.io)" to "কাস্টম হোস্টনেম (যেমন abc123.dns.nextdns.io)"),
    "save" to ("Save" to "সংরক্ষণ"),
    "invalid" to ("Enter a valid hostname, not an IP address" to "সঠিক হোস্টনেম দিন, আইপি অ্যাড্রেস নয়"),
    "open_dns" to ("Open network settings" to "নেটওয়ার্ক সেটিংস খুলুন"),
    "dns_steps" to (
        "Settings → Network & internet → Private DNS → Private DNS provider hostname → paste → Save." to
        "সেটিংস → নেটওয়ার্ক ও ইন্টারনেট → Private DNS → প্রোভাইডার হোস্টনেম → পেস্ট করুন → সেভ।"),
    "dns_restore" to ("To restore the default: set Private DNS to Automatic (or Off)." to "আগের অবস্থায় ফেরাতে Private DNS 'Automatic' বা 'Off' করুন।"),
    "dns_note" to (
        "DNS filtering cannot remove all YouTube in-stream ads. This app never changes your system DNS by itself." to
        "ডিএনএস ফিল্টার YouTube-এর সব ভিডিও-ভেতরের বিজ্ঞাপন সরাতে পারে না। এই অ্যাপ নিজে থেকে সিস্টেম ডিএনএস বদলায় না।"),
    "dns_priv" to (
        "Private DNS uses DNS-over-TLS. The provider you choose can see your DNS queries, so pick one you trust." to
        "Private DNS ব্যবহার করে DNS-over-TLS। আপনার বেছে নেওয়া প্রোভাইডার আপনার DNS কোয়েরি দেখতে পারে, তাই বিশ্বস্ত প্রোভাইডার বেছে নিন।"),
    "cur_dns" to ("Current Private DNS" to "বর্তমান Private DNS"),
    "mode_off" to ("Off" to "বন্ধ"),
    "mode_auto" to ("Automatic" to "অটোমেটিক"),
    "sel_dns" to ("Selected provider" to "নির্বাচিত প্রোভাইডার"),
    "none" to ("None" to "কিছু নেই"),
    "nextdns" to ("NextDNS: use your own private hostname from my.nextdns.io" to "NextDNS: my.nextdns.io থেকে পাওয়া নিজের হোস্টনেম ব্যবহার করুন"),
    "language" to ("Language" to "ভাষা"),
    "theme" to ("Theme" to "থিম"),
    "dark" to ("Dark" to "ডার্ক"),
    "light" to ("Light" to "লাইট"),
    "anim" to ("Animations" to "অ্যানিমেশন"),
    "reset" to ("Reset statistics" to "পরিসংখ্যান রিসেট"),
    "reset_q" to ("Delete all statistics? This cannot be undone." to "সব পরিসংখ্যান মুছবেন? এটি ফেরানো যাবে না।"),
    "yes" to ("Yes" to "হ্যাঁ"),
    "cancel" to ("Cancel" to "বাতিল"),
    "close" to ("Close" to "বন্ধ করুন"),
    "privacy" to ("Privacy policy" to "প্রাইভেসি পলিসি"),
    "privacy_text" to (
        "• No data is collected, stored off-device or uploaded.\n• No account, password or browsing history is accessed.\n• The Accessibility Service only reads the YouTube app screen to find a Skip Ads button.\n• Statistics are stored only on this phone and count real successful clicks.\n• No internet permission is used by this app." to
        "• কোনো তথ্য সংগ্রহ, ফোনের বাইরে সংরক্ষণ বা আপলোড করা হয় না।\n• কোনো অ্যাকাউন্ট, পাসওয়ার্ড বা ব্রাউজিং ইতিহাস দেখা হয় না।\n• অ্যাক্সেসিবিলিটি সার্ভিস শুধু YouTube অ্যাপের স্ক্রিনে Skip Ads বাটন খোঁজে।\n• পরিসংখ্যান শুধু এই ফোনে থাকে এবং সত্যিকারের সফল ক্লিক গণনা করে।\n• এই অ্যাপ ইন্টারনেট পারমিশন ব্যবহার করে না।"),
    "version" to ("Version" to "ভার্সন"),
    "about" to ("About" to "সম্পর্কে"),
    "about_text" to (
        "AS MASTER Ad Blocker taps the Skip Ads button when YouTube shows it. It does not modify YouTube." to
        "AS MASTER Ad Blocker YouTube-এ Skip Ads বাটন দেখা গেলে সেটি ট্যাপ করে। এটি YouTube পরিবর্তন করে না।")
)

fun tr(lang: String, key: String): String =
    T[key]?.let { if (lang == "bn") it.second else it.first } ?: key
