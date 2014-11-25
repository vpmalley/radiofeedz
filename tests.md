
# Introduction

This lists the manual tests to perform the validation of the app

# Tests

## Items list

- It should display a pic for most items, the date, the title of the item, the title of the feed. Unread items are stressed as bold font and have a small clock on the right side. Downloaded items have an attachment icon on the right.

- It should be possible to go to the end of the list without crashing.
- There should be items of multiple dates, from multiple feeds.

### Navigation drawer

- Open the drawer
- When clicking on an item, the drawer should close.

- Choosing Latest should keep all the items from multiple feeds and dates
- Choosing Today shuld display items dating from less than a day and only a time should be displayed.
- Choosing Unread should display only unread items, i.e. those in bold with a clock on the side.

### Context menu

- Multiple items can be selected

List should reflect the possible changes when:
- Marking an unread item as read
- Marking an unread item as unread
- Marking a read item as unread
- Marking a read item as read
- Archiving an item (it should disappear)
- Downloading an item (the download should start)

### Preferences

No big issue there, but when back from Preferences screen:
- The list of items is displayed
- If any preference changed, the list should show the change

### Menu actions

- adding a feed should suggest the feed to be added, with confirmation
- if the network is off, a Toast should display an error
- if the network prefs say it should not download, a Toast should display an error

- refreshing should query the feed sources and update the list with new items
- if the network is off, a Toast should display an error
- if the network prefs say it should not download, a Toast should display an error

## Feed Item Reader

### Menu actions

### Swiping

## Feed manager

### Context menu

### Clicking

