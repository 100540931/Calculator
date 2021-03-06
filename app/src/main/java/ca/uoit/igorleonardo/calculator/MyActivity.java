package ca.uoit.igorleonardo.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.content.DialogInterface;

import java.lang.reflect.Field;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;


public class MyActivity extends Activity {

    private String operators = "-+×÷";
    private final String historyTag = "historyTag";
    public ArrayList<String> history = new ArrayList<String>();
    private String currentDisplay = "";
    private final String currentDisplayTag = "currentDisplayTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // hack to force to show the action overflow button
        // even in phones with virtual navigation controls
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        if (savedInstanceState != null) {
            TextView displayText = (TextView) findViewById(R.id.display);
            currentDisplay = savedInstanceState.getString(currentDisplayTag);
            displayText.setText(currentDisplay);
            history = savedInstanceState.getStringArrayList(historyTag);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putString(currentDisplayTag, currentDisplay);
        outState.putStringArrayList(historyTag, history);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_history:
                showHistory();
                break;
            case R.id.action_about:
                showAbout();
                break;
            case R.id.action_quit:
                showQuit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public AlertDialog.Builder createDialog(final Context context, final LayoutInflater layoutInflater, final String title, final String content, final ArrayList<String> items) {
        final View dialogView = layoutInflater.inflate(R.layout.dialog, null);
        TextView dialogTitle = (TextView) dialogView.findViewById(R.id.textViewTitleDialog);
        dialogTitle.setText(title);
        TextView dialogContent = (TextView) dialogView.findViewById(R.id.textViewContentDialog);
        final ListView dialogList = (ListView) dialogView.findViewById(R.id.listViewContentDialog);
        if(content == null){
            dialogContent.setVisibility(TextView.GONE);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    context,
                    android.R.layout.simple_list_item_1,
                    items );

            dialogList.setAdapter(arrayAdapter);
            dialogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                    View displayView = findViewById(R.id.activity_my);
                    String item = dialogList.getItemAtPosition(position).toString();
                    TextView display = (TextView) displayView.findViewById(R.id.display);
                    currentDisplay = (item.split(" = "))[0];
                    display.setText(currentDisplay);

                }
            });
        } else {
            dialogList.setVisibility(ListView.GONE);
            dialogContent.setText(Html.fromHtml(content), TextView.BufferType.SPANNABLE);
        }
        return new AlertDialog.Builder(context).setView(dialogView);
    }

    private void showQuit() {
        AlertDialog.Builder myDialog =
                createDialog(
                        this,
                        getLayoutInflater(),
                        "Confirm Exit...",
                        "Are you sure you want to close the Igor's Calculator?",
                        null
                );
        myDialog
                .setPositiveButton(getString(R.string.yes).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no).toUpperCase(), null)
                .show();
    }


    private void showHistory() {
        AlertDialog.Builder myDialog =
                createDialog(
                        this,
                        getLayoutInflater(),
                        "History",
                        null,
                        history
                );
        myDialog
                .setPositiveButton(getString(R.string.close).toUpperCase(), null)
                .show();
    }

    private void showAbout() {
        String htmlMessage = "<p>This applications was created as a pre-requisite of my "
                + "Mobile Device (CSCI 4100) course at UOIT. </p>"
                + "<p>For this assignment I had to create a <b>simple Calculator</b> capable "
                + "to solve all the <b>four basic operations</b>.</p>"
                + "<p>Enjoy it!</p>"
                + "<b>Igor Melo</b>, <i>100540931</i>";

        AlertDialog.Builder myDialog =
                createDialog(
                        this,
                        getLayoutInflater(),
                        "About",
                        htmlMessage,
                        null
                );
        myDialog
                .setNegativeButton(getString(R.string.close).toUpperCase(), null)
                .show();
    }

    public void buttonPressed(View view) {
        Button bPressed = (Button)view;
        String bValue = bPressed.getText().toString();
        String lastInput = getLastInput();
        TextView displayText = (TextView) findViewById(R.id.display);

        // "point" control
        // handle multiples attempts to input "."
        if(bValue.equals(".")){
            String[] exprs = getExpressions(currentDisplay);
            String lastExpr = exprs[exprs.length-1];
            if(checkPoint(lastExpr)){
                if(lastInput.equals("") || operators.contains(lastInput)){
                    displayText.append("0");
                }
            } else bValue = "";
        }

        // operators control
        // handle operators to avoid malformed input
        if(!bValue.isEmpty() && operators.contains(bValue)){
            if(!bValue.equals("-") && (currentDisplay.equals("-") || currentDisplay.equals(""))){
                backspace(view);
                bValue = "";
            } else {
                if(currentDisplay.length()>2 && (getLastInput(2).contains("÷-") || getLastInput(2).contains("×-")))
                    backspace(view);
                if((operators.contains(lastInput) && !((lastInput.equals("×") || lastInput.equals("÷")) && bValue.equals("-"))) || lastInput.equals("."))
                    backspace(view);
            }
        }

        // update display and current display varibale
        displayText.append(bValue);
        currentDisplay = displayText.getText().toString();
    }

    public void result(View view) {
        Float number1, number2, result;
        TextView displayText = (TextView) findViewById(R.id.display);
        ArrayList<String> exprs;

        exprs = treatExpressions(getExpressions(currentDisplay));

        result = null;
        while(exprs.size() != 1){
            int i;

            // Operator order: Multiplication, Division, Addition, Subtraction
            if((i=exprs.indexOf("×")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                result = number1 * number2;
            } else if((i=exprs.indexOf("÷")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                result = number1 / number2;
            } else if((i=exprs.indexOf("+")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                result = number1 + number2;
            } else if((i=exprs.indexOf("-")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                result = number1 - number2;
            }

            // if result is different of null, perform update array list
            if(result!=null) {
                exprs.set(i, Float.toString(result));
                exprs.remove(i + 1);
                exprs.remove(i - 1);
                result = null;
            }
        }

        // Check if result is a Interger
        result = Float.parseFloat(exprs.get(0));
        if (result == result.intValue()) {
            exprs.set(0, Integer.toString(result.intValue()));
        } else exprs.set(0, String.format("%.2f", result));

        // Update display
        displayText.setText(exprs.get(0));

        // update history
        history.add(currentDisplay + " = " + exprs.get(0));

        // update current display variable
        currentDisplay = exprs.get(0);
    }

    private ArrayList<String> treatExpressions(String[] exprs) {
        ArrayList<String> expr1 = new ArrayList<String>(Arrays.asList(exprs));
        int size;

        // Check and remove first empty element
        if(expr1.get(0).isEmpty())
            expr1.remove(0);

        // Check if first element is a negative signal and concatenate it with the next elment
        if(expr1.get(0).equals("-")) {
            expr1.set(1, "-".concat(expr1.get(1)));
            expr1.remove(0);
        }

        // Trim right unused operators
        size = expr1.size();
        while(operators.contains(expr1.get(size-1))){
            expr1.remove(size-1);
            size = expr1.size();
        }

        // Concatenate negative signal with the respective following number
        for(int i = expr1.indexOf("-"); i!=-1 && i<size; i++){
            String actual = expr1.get(i);
            String prev = expr1.get(i-1);

            // if the element is a negative signal and the previous one is
            // a operator for multiplication or division, concatenate it properly
            if(actual.equals("-") && (prev.equals("×") || prev.equals("÷"))){
                expr1.set(i+1, actual.concat(expr1.get(i+1)));
                expr1.remove(i);
                size--;
            }
        }

        // Return well formatted expressions
        return expr1;
    }

    public void backspace(View view) {
        TextView displayText = (TextView) findViewById(R.id.display);

        if(!currentDisplay.equals("")) {
            displayText.setText(currentDisplay.substring(0, currentDisplay.length() - 1));
            currentDisplay = displayText.getText().toString();
            if(currentDisplay.length() == 0) {
                reset(view);
            }
        }
    }

    public void reset(View view) {
        TextView displayText = (TextView) findViewById(R.id.display);
        displayText.setText("");
        currentDisplay = "";
    }

    private String[] getExpressions(String equation){
        String regex = "(?<=op)|(?=op)".replace("op", "["+operators+"]");
        return equation.split(regex);
    }

    private boolean checkPoint(String expr){
        int count = expr.length() - expr.replace(".", "").length();
        return count!=1;
    }

    public String getLastInput(){
        return (currentDisplay.length() >= 1) ? currentDisplay.substring(currentDisplay.length() - 1, currentDisplay.length()) : "";
    }

    public String getLastInput(int length){
        return (currentDisplay.length() >= length) ? currentDisplay.substring(currentDisplay.length() - length, currentDisplay.length()) : getLastInput();
    }
}
