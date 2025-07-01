package simplex_method;

import javax.swing.*;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class SimplexGUI {
    private JFrame frame;
    private JTextArea inputArea;
    private JComboBox<String> optimizationType;
    private JTextArea outputArea;
    
    private boolean isMinimization;

    public SimplexGUI() {
        // Initialize the frame
        frame = new JFrame("Simplex Method Solver");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());
        
        //Shared panel for input and hint
        JPanel topPanel =new JPanel(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));

        optimizationType = new JComboBox<>(new String[]{"Maximization", "Minimization"});
        inputPanel.add(optimizationType, BorderLayout.NORTH);
        // Hint panel
        JPanel hintPanel = new JPanel(new BorderLayout());
        hintPanel.setBorder(BorderFactory.createTitledBorder("Input Format")); //This is not displaying

        JTextArea hintArea = new JTextArea();
        hintArea.setEditable(false);
        hintArea.setText("Objective Func \t 60x1+40x2 \n"
        		+ "Constraint #1 \t x1+x2>=150\n"
        		+ "Constraint #2 \t x1-2x2>=0\n"
        		+ "Constraint #3... \t .........");
        hintArea.setLineWrap(true);
        hintArea.setWrapStyleWord(true);
        hintPanel.add(hintArea, BorderLayout.CENTER);

        topPanel.add(hintPanel,BorderLayout.NORTH);
        
        //
        
        inputArea = new JTextArea(10, 40);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        
        topPanel.add(inputPanel,BorderLayout.CENTER);
        
        frame.add(topPanel, BorderLayout.NORTH);

        // Output panel
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));

        outputArea = new JTextArea(15, 40);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);

        frame.add(outputPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(new SolveButtonListener());
        buttonPanel.add(solveButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    class SolveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                isMinimization = optimizationType.getSelectedItem().toString().equalsIgnoreCase("Minimization");
                String[] inputLines = inputArea.getText().split("\n");
                ArrayList<String> inputList = new ArrayList<>();
                for (String line : inputLines) {
                    if (!line.trim().isEmpty()) {
                        inputList.add(line.trim());
                    }
                }
                
                String[] problemInput = inputList.toArray(new String[0]);
             
               
                double[][] matrix = parseInput(problemInput);
                simplex simplexSolver = new simplex(matrix, isMinimization);
                simplexSolver.solve();

                String outputLoops =simplexSolver.getOutput();
                double optimalValue = simplexSolver.getOptimalValue();
                double[] solution = simplexSolver.getSolution();

                // Display output
                StringBuilder output = new StringBuilder();
                output.append(outputLoops);
                
                output.append("Optimal Value: ").append(String.format("%.2f",optimalValue)).append("\n\nSolution:\n");
                for (int i = 0; i < solution.length; i++) {
                    output.append("x").append(i + 1).append(" = ").append(String.format("%.2f",solution[i])).append("\n");
                }
                outputArea.setText(output.toString());
            } catch (Exception ex) {
            	ex.printStackTrace();//**DELETE LATER
                outputArea.setText("Error: " + ex.getMessage());
            }
        }
     // Class to hold parsed constraints
        public class ParsedConstraint {
            double[] coefficients;
            String constraintType;
            double rhs;

           public ParsedConstraint(double[] coefficients, String constraintType, double rhs) {
                this.coefficients = coefficients;
                this.constraintType = constraintType;
                this.rhs = rhs;
            }
        }

        private double[][] parseInput(String[] problemInput) {
            // Parse the objective function
            
            double[] objFunc = parseObjective(problemInput[0]);
            

            // Parse constraints
            int numConstraints = problemInput.length - 1;
            int numVars = objFunc.length;
            double[][] table = new double[numConstraints + 1][numVars + 1];

            for (int i=1;i<problemInput.length;i++) {
            	String cons =problemInput[i];
            	ParsedConstraint pCons = parseConstraint(cons);
            	for (int j=0;j<(pCons.coefficients.length);j++) { //objFunc + rhs for constraints
            		table [i-1][j]=pCons.coefficients[j];
            	}
            	table[i-1][pCons.coefficients.length]=pCons.rhs;//add right hand side value
            }
            for (int i=0;i<objFunc.length;i++) {
            	table[numConstraints][i]=objFunc[i];
            }
          
            System.out.println("ORIGINAL TABLE\n");
            for (int i=0;i<table.length;i++) {
            	System.out.println(Arrays.toString(table[i]));//debugging ***
            }
            
            
            return table;
        }
        
        public double[] parseObjective(String input) {
            return parseCoefficients(input,false); //objective only has coefficients
        }

		private ParsedConstraint parseConstraint(String constraint) {
			    boolean mixed=false;
			    Pattern operatorPattern = Pattern.compile("(<=|>=|=)"); 
		        Matcher operatorMatcher = operatorPattern.matcher(constraint);
		        String operator = null;
		        
		       
		        
		        if (operatorMatcher.find()) {
		            operator = operatorMatcher.group();
		        }

		        // Split into left-hand side and right-hand side
		        //mixed constraints, multiply by -1 if not matching the problem type *****
		        if (operator.equals(">=")&&isMinimization==true||operator.equals("<=")&&isMinimization==false) { ////////////////////////////////****
		        	
		        	
		        	String[] parts = constraint.split("(<=|>=|=)");
			        String lhs = parts[0].trim();
			        double rhs = Double.parseDouble(parts[1].trim()); 

			        // Parse coefficients from the left-hand side
			        double[] coefficients = parseCoefficients(lhs,mixed);

			        return new ParsedConstraint(coefficients, operator, rhs);
		        }
		        else {
		        	mixed=true;
		        	//multiply all values by negative 1
		        	String[] parts = constraint.split("(<=|>=|=)");
			        String lhs = parts[0].trim();
			        double rhs = Double.parseDouble(parts[1].trim())*-1; 

			        // Parse coefficients from the left-hand side
			        double[] coefficients = parseCoefficients(lhs,mixed);

			        return new ParsedConstraint(coefficients, operator, rhs);
		        }
		        
		        
		}

		public double[] parseCoefficients(String input,boolean mixed) {
		    // Regex to match terms like "3x1", "-2x2", or "x1"
		    Pattern termPattern = Pattern.compile("([+-]?\\d*\\.?\\d*)x(\\d+)");
		    Matcher matcher = termPattern.matcher(input);

		    // Map to store coefficients for each variable
		    Map<Integer, Double> coefficientsMap = new HashMap<>();
		    while (matcher.find()) {
		        String coefficientStr = matcher.group(1);
		        int variableIndex = Integer.parseInt(matcher.group(2)) - 1; // Convert x1, x2, ... to array index

		        // Parse the coefficient, default to 1 or -1 if missing
		        double coefficient = coefficientStr.isEmpty() || coefficientStr.equals("+") ? 1.0 :
		                             coefficientStr.equals("-") ? -1.0 :
		                             Double.parseDouble(coefficientStr);

		        coefficientsMap.put(variableIndex, coefficient);

		        // Debugging output
		        System.out.println("Matched term: " + matcher.group() + ", Coefficient: " + coefficient + ", Variable Index: " + variableIndex);//***
		    }

		    // Determine the number of variables
		    int numVariables = coefficientsMap.keySet().stream().max(Integer::compare).orElse(0) + 1;

		    // Create an array of coefficients
		    
		    //if it is a mixed constraint then multiply all values by -1
		    double[] coefficients = new double[numVariables];
		    if (mixed==true) {
		    	
			    for (int i = 0; i < numVariables; i++) {
			        coefficients[i] = coefficientsMap.getOrDefault(i, 0.0)*-1;
			    }
		    }
		    else {
			    for (int i = 0; i < numVariables; i++) {
			        coefficients[i] = coefficientsMap.getOrDefault(i, 0.0);
			    }

		    }
		    
		    // Debugging output
		    System.out.println("Coefficients array: " + Arrays.toString(coefficients));//***

		    return coefficients;
		}
    }
    

    public static void main(String[] args) {
        new SimplexGUI();
    }
}
