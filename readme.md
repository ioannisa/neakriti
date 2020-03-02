# NeaKriti News App
## An advanced news reader
*by Ioannis Anifantakis*

NeaKriti Android News App is a news application based on the neakriti.gr news agency network.
[https://www.neakriti.gr](https://www.neakriti.gr)

## Intended User

The app is intended for the average greek news reader, giving him the ability to explore local news from the neakriti.gr news agency.


## Features

* Displays news Articles (html formatted) in a WebView via downloaded Feeds
* Allows Users to **bookmark favorite news articles for later reading**
* Supports **offline reading** with the topmost 20 articles from each category
* **Firebase Remote Configuration** for various changes without the need to reinstall the app
* **Firebase Cloud Messaging (FCM)** will enable push notifications for
	* Important Articles opened directly on Application
	* Things other than articles that need to open on Browser instead
	* Important update notifications targeting specific problematic builds
* **Text to Speech (TTS)** on articles content to aid visually impaired people
* **Live Streaming Services** of neakriti subsidiary companies
	* **Live Radio Stream** service of the “Radio984” radio station with **spotify-like notification system**
	* **Live TV Stream** of CreteTV station with **pinch-to-zoom gestures** and **Chromecast support**
* Day and Night **themes**
* **Various article display types** (Articles List, Details List, Card View)
* **Preferences** regarding text size, theme, article categories, etc
* **Targeting Phone and Tablets** via fragments
* Material Design principles
* Home Screen **Widget** showing the top articles
* More **Google Services** like Google Ads
* **Article Sharing**
* Allows for **multi-langual menus and in-app content** (Greek - English)


## Accessibility

There are some accessibility features (included in the above features list) that will help certain user groups.

* Text-to-Speech on articles vis eSpeak (where available) will help the visually impaired on article content reading.
* Variable Font sizes (specified inside the preferences fragment) will allow users to change the font size for the content of the articles to allow more comfort while reading.
* The availability of day and night themes will also provide some extra comfort to the users and the dark theme may be more relaxing for people sensitive to bright lights.

## Screenshots

#### Transition from Day to Night Theme
![Transition from Day to Night Theme](https://services.anifantakis.eu/github/neakriti/screenshots/day_night.gif "Transition from Day to Night Theme")

#### Opening Live Streaming Panel
![Opening Live Streaming Panel](https://services.anifantakis.eu/github/neakriti/screenshots/live.gif "Opening Live Streaming Panel")

#### Drawer Menu
![Drawer Menu](https://services.anifantakis.eu/github/neakriti/screenshots/menu.gif "Drawer Menu")

#### Shared Element Transitions
![Shared Element Transitions](https://services.anifantakis.eu/github/neakriti/screenshots/transition.gif "Shared Element Transitions")

#### Firebase Cloud Messaging (FCM) Notifications
![Firebase Cloud Messaging (FCM) Notifications](https://services.anifantakis.eu/github/neakriti/screenshots/notification.png "Firebase Cloud Messaging (FCM) Notifications")

#### Main Screen Widget
![Main Screen Widget](https://services.anifantakis.eu/github/neakriti/screenshots/widget.png "Main Screen Widget")