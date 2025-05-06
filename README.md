# CdezNETMessaging
Available as another way to interact with the Cdez.net messaging service.
## **Features**

1. **Message Sending:**
   - Users can send messages to a chatroom.
   - Messages include the username, chatroom name, and a timestamp.
   - Messages are sent to a server using a `Retrofit` API call.

2. **Message Fetching:**
   - Periodically fetches messages from the server every second using a `Handler` and `Runnable`.
   - Parses and processes messages received from the server.

3. **Message Storage:**
   - Messages are stored in a local database using a `DatabaseHelper`.
   - Messages are loaded from the database and displayed in a `RecyclerView`.

4. **Dynamic Chatroom Switching:**
   - Users can switch between chatrooms using a menu option.
   - Messages are cleared and reloaded for the selected chatroom.

5. **Clear Messages:**
   - Users can clear all messages currently displayed in the `RecyclerView` using a "Clear Messages" menu option.

6. **User Preferences:**
   - Stores user preferences (e.g., server address, username, chatroom) in `SharedPreferences`.
   - Preferences are loaded when the app starts.

7. **Customizable Server Address:**
   - Users can change the server address via a menu option.

8. **Customizable Username:**
   - Users can change their username via a menu option.

9. **Dynamic UI Adjustments:**
   - Adjusts padding dynamically to account for system bars (e.g., status bar, navigation bar) using `WindowInsetsCompat`.

10. **App Bar and Status Bar Customization:**
    - The app bar and status bar colors are set to `#2D7DD2`.
    - Ensures proper contrast for text and icons on the status bar.

11. **RecyclerView Features:**
    - Displays messages in a scrollable list.
    - Automatically scrolls to the newest message when new messages are added.

12. **Error Handling:**
    - Displays `Toast` messages for errors (e.g., failed API calls, empty input fields).

13. **Navigation Drawer:**
    - Includes a navigation drawer with menu options for changing settings and clearing messages.

14. **Input Validation:**
    - Ensures that messages, server addresses, and usernames are not empty before saving or sending.

15. **Material Design Components:**
    - Uses `Material Design` components such as `AppBarLayout`, `Toolbar`, and `NavigationView`.
