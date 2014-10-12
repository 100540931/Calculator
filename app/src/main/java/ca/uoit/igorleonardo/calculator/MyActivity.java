package ca.uoit.igorleonardo.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;


public class MyActivity extends Activity {

    private String currentDisplay = "";
    private final String currentDisplayTag = "";
    private String operators = "-+×÷";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState != null) {
            currentDisplay = savedInstanceState.getString(currentDisplayTag);
            TextView textView = (TextView) findViewById(R.id.display);
            textView.setText(currentDisplay);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putString(currentDisplayTag, currentDisplay);
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void buttonPressed(View view) {
        Button bPressed = (Button)view;
        String bValue = bPressed.getText().toString();
        String lastInput = getLastInput();
        TextView displayText = (TextView) findViewById(R.id.display);

        // "point" control
        if(bValue.equals(".")){
            String[] exprs = getExpressions(currentDisplay);
            String lastExpr = exprs[exprs.length-1];
            if(checkPoint(lastExpr)){
                if(getLastInput().equals("") || operators.contains(lastInput)){
                    displayText.append("0");
                }
            } else bValue = "";
        }

        // operators control
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

        displayText.append(bValue);
        currentDisplay = displayText.getText().toString();
    }

    public void result(View view) {
        float number1, number2, result;
        TextView displayText = (TextView) findViewById(R.id.display);
        ArrayList<String> exprs;

        exprs = treatExpressions(getExpressions(currentDisplay));

        while(exprs.size() != 1){
            int i;
            while ((i=exprs.indexOf("×")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                exprs.set(i, Float.toString(number1 * number2));
                exprs.remove(i+1);
                exprs.remove(i-1);
            }
            while ((i=exprs.indexOf("÷")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                exprs.set(i, Float.toString(number1 / number2));
                exprs.remove(i+1);
                exprs.remove(i-1);
            }
            while ((i=exprs.indexOf("+")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                exprs.set(i, Float.toString(number1 + number2));
                exprs.remove(i+1);
                exprs.remove(i-1);
            }
            while ((i=exprs.indexOf("-")) != -1) {
                number1 = Float.parseFloat(exprs.get(i-1));
                number2 = Float.parseFloat(exprs.get(i+1));
                exprs.set(i, Float.toString(number1 - number2));
                exprs.remove(i+1);
                exprs.remove(i-1);
            }
        }

        /*switch(operator){
            case '+':
                displayText.setText(Float.toString(number1 + number2));
                break;
            case '-':
                displayText.setText(Float.toString(number1-number2));
                break;
            case '×':
                displayText.setText(Float.toString(number1*number2));
                break;
            case '÷':
                if(number2 == 0)
                    displayText.setText(DecimalFormatSymbols.getInstance().getInfinity());
                else {
                    displayText.setText(Float.toString(number1 / number2));
                }
                break;
        }*/

        result = Float.parseFloat(exprs.get(0));
        if (result == (int)result) {
            exprs.set(0, Integer.toString((int)result));
        }

        displayText.setText(exprs.get(0));
        currentDisplay = exprs.get(0);
    }

    private ArrayList<String> treatExpressions(String[] exprs) {
        ArrayList<String> expr1 = new ArrayList<String>(Arrays.asList(exprs));

        if(expr1.get(0).isEmpty())
            expr1.remove(0);
        if(expr1.get(0).equals("-")) {
            expr1.set(1, "-".concat(expr1.get(1)));
            expr1.remove(0);
        }

        int size = expr1.size();

        for(int i = expr1.indexOf("-"); i!=-1 && i<size; i++){
            String actual = expr1.get(i);
            String prev = expr1.get(i-1);

            if(actual.equals("-") && (prev.equals("×") || prev.equals("÷"))){
                expr1.set(i+1, actual.concat(expr1.get(i+1)));
                expr1.remove(i);
                size--;
            }
        }

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
