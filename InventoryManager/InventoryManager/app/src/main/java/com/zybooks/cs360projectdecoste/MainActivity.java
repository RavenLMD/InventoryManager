package com.zybooks.cs360projectdecoste;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//Main application activity, extends application activity class AppCompatActivity
public class MainActivity extends AppCompatActivity {

    //Used for the inventory management screen's buttons. An array list for the 4 reusable item buttons on the inventory screen
    ArrayList <Integer> itemButtons = new ArrayList<Integer>();

    //Code for requesting text permissions
    private final int REQUEST_TEXT_CODE = 0;

    //Database variables for inventory and users
    objectValueDatabase userDatabase;
    objectValueDatabase inventoryDatabase;

    //Edit text widgets for collection of usernames and passwords
    EditText myEditTextUser;
    EditText myEditTextPass;

    //The index of the default inventory screen. Used to determine which items to show on the screen, allowing for navigation of inventory database
    int itemScreenIndex = 1;

    //Runs when main activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checks for texting permission
        hasFilePermissions();

        //Populates list of item button IDs
        //Button IDs are used to programmatically modify and use buttons on the inventory management screen
        itemButtons.add(R.id.item1);
        itemButtons.add(R.id.item2);
        itemButtons.add(R.id.item3);
        itemButtons.add(R.id.item4);

        //Defines and sets objects for interacting with SQL databases
        objectValueDatabase userDatabaseNew = new objectValueDatabase(getApplicationContext(), "users.db");
        objectValueDatabase inventoryDatabaseNew = new objectValueDatabase(getApplicationContext(), "items.db");
        userDatabase = userDatabaseNew;
        inventoryDatabase = inventoryDatabaseNew;

        //If no database exists, create one with 4 items. Each inventory screen contains up to 4 items at a time, so 4 default items are used
        if(inventoryDatabase.getDBTableCount()<4) {
            inventoryDatabase.createObject("item 1", "0");
            inventoryDatabase.createObject("item 2", "0");
            inventoryDatabase.createObject("item 3", "0");
            inventoryDatabase.createObject("item 4", "0");
        }
    }

    //Checks for texting permissions and asks for them if needed
    private boolean hasFilePermissions() {
        //Gets textPermission status
        String textPermission = Manifest.permission.SEND_SMS;
        //Asks for permission if required
        if (ContextCompat.checkSelfPermission(this, textPermission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, textPermission)) {
            }
            //If permission isn't granted, asks for permission to send texts
            else {
                ActivityCompat.requestPermissions(this, new String[]{textPermission}, REQUEST_TEXT_CODE);
            }
            return false;
        }
        return true;
    }

    //Switches to inventory management screen
    public void switchScreen(View view) {
        setContentView(R.layout.inventory_grid);
    }

    //Sends test text to user from phone number (555)-555-5555 if permissions allow
    public void sendText(View view){
        String textPermission = Manifest.permission.SEND_SMS;
        //Only attempts to send if permissions allow
        if(ContextCompat.checkSelfPermission(this, textPermission) == PackageManager.PERMISSION_GRANTED) {
            String message = "test message";
            //Number from which the message will come
            String number = "5555555555";
            //Sends text with default SmsManager
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number,null, message,null,null);
        }
    }

    //Adds usernames and passwords to database
    public void addUser(View view) {
        //Edit text widgets used for entering credentials
        myEditTextUser = (EditText) findViewById(R.id.username);
        myEditTextPass = (EditText) findViewById(R.id.password);
        //Strings for containing credentials
        String username = myEditTextUser.getText().toString();
        String password = myEditTextPass.getText().toString();

        //If credentials are invalid
        if (username.length() == 0 || password.length() == 0){ //|| i != -1){
            Toast.makeText(MainActivity.this, "User not added! Invalid user!", Toast.LENGTH_SHORT).show();
        }
        //If credentials are valid
        else {
            userDatabase.createObject(username, password);
            Toast.makeText(MainActivity.this, "User added!", Toast.LENGTH_SHORT).show();
        }
    }

    //Determines if credentials are valid, and if so, brings the user to the inventory management screen
    public void loginUser(View view) {
        //Edit text widgets used for entering credentials
        myEditTextUser = (EditText) findViewById(R.id.username);
        myEditTextPass = (EditText) findViewById(R.id.password);
        //Strings for containing credentials
        String username = myEditTextUser.getText().toString();
        String password = myEditTextPass.getText().toString();

        //Variable for defining if a credential check is passed
        boolean credCheck = userDatabase.checkValueByObject(username, password);

        //Switch screen to inventory manager if credentials are valid
        if (credCheck == true) {
            setContentView(R.layout.inventory_grid);
            refreshScreen();
        }
        //If user does not exist, alert user and do not sign-in
        else{
            Toast.makeText(MainActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
        }
    }

    //Requests permission to send user text messages
    public void askPermission(View view) {
        String textPermission = Manifest.permission.SEND_SMS;
        ActivityCompat.requestPermissions(this, new String[]{textPermission}, REQUEST_TEXT_CODE);
    }

    //Changes inventory management screen to show next or previous set of inventory items
    //Utilizes itemScreenIndex to determine which set of up to 4 items to show. Starting index is 1
    public void changeScreen(View view){
        //Variable for determining if the inventory management screen is to show next or previous items
        int direction=0;
        //If the button pressed was the back button, direction is negative
        if (view.getId() == R.id.left){
            direction=-1;
        }
        //If the button pressed was the next button, direction is positive
        else if (view.getId() == R.id.right){
            direction=1;
        }

        //Sets itemScreenIndex to next or previous
        itemScreenIndex = itemScreenIndex + direction;

        //If itemScreenIndex became an illegal value, undoes change to its value
        //If itemScreenIndex is out of bounds (too low)
        if (itemScreenIndex < 1){
            itemScreenIndex = 1;
        }
        //If itemScreenIndex is out of bounds (too high, would have no items to show)
        //lowestItemIndexOnScreen used to determine if transitioning to the next screen index will show at least 1 item on the screen
        //As each inventory screen shows 4 item buttons, itemScreenIndex is multiplied by 4. 3 is subtracted to find the lowest indexed item in that set of 4 buttons
        int lowestItemIndexOnScreen = (itemScreenIndex*4) - 3;
        if (lowestItemIndexOnScreen > inventoryDatabase.getDBTableCount()){
            itemScreenIndex--;
        }
        refreshScreen();
    }

    //Returns cursor for database at specific index
    public Cursor cursorToIndex(int index, objectValueDatabase db){
        //Gets database cursor and moves it to the start of the table
        Cursor tempCursor = db.getDB();
        tempCursor.moveToNext();

        //Moves cursor to next until requested index is reached
        for (int i = 1; i < index; i++){
                tempCursor.moveToNext();
        }
        return tempCursor;
    }

    //Refreshes inventory management screen visuals so as to remain consistent with information in the database
    public void refreshScreen() {
        //Gets the 4 item buttons
        Button button1 = (Button) findViewById(itemButtons.get(0));
        Button button2 = (Button) findViewById(itemButtons.get(1));
        Button button3 = (Button) findViewById(itemButtons.get(2));
        Button button4 = (Button) findViewById(itemButtons.get(3));

        //Gets cursor and moves it into first position
        Cursor tempCursor = inventoryDatabase.getDB();
        tempCursor.moveToNext();

        //Cursor move next 4 times for each index (because 4 items are displayed on the screen at a time)
        for (int i = 1; i < itemScreenIndex; i++){
            for (int j = 1; j <=4; j++){
                tempCursor.moveToNext();
            }
        }

        //Instantiates itemName and itemCount variables, which will be used to set the text on the item buttons
        String itemName1, itemName2, itemName3, itemName4, itemCount1, itemCount2, itemCount3, itemCount4;
        //Gives itemName and itemCount variables empty default values
        itemName1 = itemName2 = itemName3 = itemName4 = itemCount1 = itemCount2 = itemCount3 = itemCount4 = " ";

        //Item index is used to determine if each item button is to display an item
        //If itemIndex is below the amount of items in the database, no item is displayed for the button
        int itemIndex = 1 + (itemScreenIndex-1)*4;
        int amountOfItems = inventoryDatabase.getDBTableCount();

        //Displays inventory item on first button if itemIndex is below amountOfItems
        if (itemIndex <= amountOfItems) {
            itemName1 = tempCursor.getString(0);
            itemCount1 = tempCursor.getString(1);
            itemIndex++;
            tempCursor.moveToNext();
        }
        //Displays inventory item on second button if itemIndex is below amountOfItems
        if (itemIndex <= amountOfItems) {
            itemName2 = tempCursor.getString(0);
            itemCount2 = tempCursor.getString(1);
            itemIndex++;
            tempCursor.moveToNext();
        }
        //Displays inventory item on third button if itemIndex is below amountOfItems
        if (itemIndex <= amountOfItems) {
            itemName3 = tempCursor.getString(0);
            itemCount3 = tempCursor.getString(1);
            itemIndex++;
            tempCursor.moveToNext();
        }
        //Displays inventory item on fourth button if itemIndex is below amountOfItems
        if (itemIndex <= amountOfItems) {
            itemName4 = tempCursor.getString(0);
            itemCount4 = tempCursor.getString(1);
        }
        //Updates button displays
        button1.setText(itemName1 + ": " + itemCount1);
        button2.setText(itemName2 + ": " + itemCount2);
        button3.setText(itemName3 + ": " + itemCount3);
        button4.setText(itemName4 + ": " + itemCount4);
    }

    //Adds item to inventory database
    public void addItem(View view){
        //Counts how many items exist
        int count = inventoryDatabase.getDBTableCount();
        //Generates item name based on how many items exist
        String stringCount = Integer.toString(count + 1);
        String itemName = "Item " + stringCount;

        //Creates new inventory item with default amount of 0
        inventoryDatabase.createObject(itemName, "0");

        //Updates display with current database information
        refreshScreen();
    }

    public void changeCount(View view){
        //Gets tag representing which item the button belongs to from xml and converts it to a string
        String itemButtonNumber = view.getTag().toString();
        //Converts string item number into integer form
        int intOfItemButton = Integer.parseInt(itemButtonNumber);
        //itemIndex determined by adding the button's tag value (intOfItemButton) to the amount of items represented by the current itemScreenIndex
        //1 is subtracted from itemScreenIndex, as on index 1, there are no previous items
        //4 is multiplied against itemScreenIndex as there are 4 items per screen
        int itemIndex = intOfItemButton + (itemScreenIndex-1)*4;

        int amountOfItems = inventoryDatabase.getDBTableCount();

        //Gets correct button for visual updates (-1 as the first index is 0, not 1)
        Button button = (Button)findViewById(itemButtons.get(intOfItemButton-1));

        //Variable for determining if the item's stock is being added to, subtracted from, or removed. Defaults to 0 for removal
        int addOrSub=0;
        //If the button pressed was an add button, addOrSub is positive
        if (view.getId() == R.id.add){
            addOrSub=1;
        }
        //If the button pressed was a subtract button, addOrSub is negative
        else if (view.getId() == R.id.subtract){
            addOrSub=-1;
        }

        //If button pressed does represent on item, modify item's count and update display, otherwise, do not
        if (itemIndex <= amountOfItems) {
            //Gathers inventory item information using index
            Cursor itemCursor = cursorToIndex(itemIndex, inventoryDatabase);
            String itemName = itemCursor.getString(0);
            String itemCount = itemCursor.getString(1);

            //Variables for the collection of the new item total
            int newCount = 0;
            String newCountString = "0";

            //If the remove button is pressed, newCount remains at 0
            //Adds or removes 1 item from stock
            if (addOrSub != 0) {
                newCount = Integer.parseInt(itemCount) + addOrSub;
                if (newCount < 0) {
                    newCount = 0;
                }
                newCountString = String.valueOf(newCount);
            }
            //Sets new item count
            inventoryDatabase.updateObject(itemName, newCountString);
            //Displays new item count, applies visual update
            refreshScreen();
        }
    }

    //WARNING: DANGEROUS
    //Callback function for deleting contents of entire table in database
    public void deleteAllItems(View view){
        inventoryDatabase.deleteAll();
    }
}