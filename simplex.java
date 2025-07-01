package simplex_method;

import java.util.Arrays;

public class simplex {
	private double[][] table;
	private int numberOfConstraints;
	private int numberOfVariables;
	private int numberOfVariablesOriginal; //if a transpose method is done this will store the original number of variables to later show them in the solution
	private boolean isMinimization; //To determine if to minimize or maximize 
	private int loopNum;
	
	private StringBuilder outputBuilder; //This will be used to get the final output of the program
	
	
	public simplex(double[][] matrix, boolean problemType) {
		this.outputBuilder = new StringBuilder(); //Initialize the output builder
		this.numberOfConstraints = matrix.length-1;//minus the objective function
        this.numberOfVariables = matrix[0].length-1;
        this.numberOfVariablesOriginal =matrix[0].length-1;
        this.isMinimization = problemType;
        
        table = new double[numberOfConstraints + 1][numberOfVariables + numberOfConstraints + 1];//variables + slack variables + rhs
        
        // Print the original matrix
        	outputBuilder.append("Original Matrix: \n");
	    for (double[]row:matrix) {
	    	
	    	outputBuilder.append(Arrays.toString(row)).append("\n");
	    }
	    outputBuilder.append("\n");
        if (isMinimization==true) {
        	
        	transpose(matrix);//Fill the table but by transposing it to solve it as a maximization problem
        }
        else {
        	
        	initializeTable(matrix);// Fill the table
        }  
	}
	
	
	//transpose to Max if it is a min problem FIRST create the matrix, then transpose, then work with it
	 // Transpose to Max if it is a min problem
	public void transpose(double[][] matrix) {
	    // Get the dimensions of the original matrix
	    int rows = matrix.length;
	    int cols = matrix[0].length;

	    // Create a new matrix for the transpose
	    double[][] transposed = new double[cols][rows];

	    // Fill the transposed matrix
	    for (int i = 0; i < rows; i++) {
	        for (int j = 0; j < cols; j++) {
	            transposed[j][i] = matrix[i][j];
	        }
	    }
	    // Print the transposed matrix
	    outputBuilder.append("Transposed Matrix:\n ");
	    for (double[] row : transposed) {
	    	 outputBuilder.append(Arrays.toString(row)).append("\n");
	    }
	    outputBuilder.append("\n");
	    matrix=transposed;
	    
		makeMaximize(matrix);
		
	}
	
	public void makeMaximize(double[][] matrix) {
		this.numberOfConstraints = matrix.length-1;//minus the objective function
        this.numberOfVariables = matrix[0].length-1;//minus the rhs
        //re-scale the table to the transposed matrix
        table = new double[numberOfConstraints + 1][numberOfVariables + numberOfConstraints + 1];
		initializeTable(matrix);	
	}


	public void initializeTable (double[][]matrix) {
		//adding constraints
		for (int i=0;i<numberOfConstraints;i++) {
			for (int j=0;j<numberOfVariables;j++) {
				table[i][j]=matrix[i][j];
			}
		}
		
		//slacks
		int slackPos=numberOfVariables;
		for (int i=0;i<numberOfConstraints;i++) {
			for (int j=numberOfVariables;j<table[0].length-1;j++) { //now add slacks after variables
				table[i][slackPos]=1.0;//Slack pos will add the value in one row and to the next one add 1 to the next column of the next row
				slackPos++;
				break;
			}
		}
		//rhs
		for (int i=0;i<numberOfConstraints;i++) {
			table[i][table[0].length-1]=matrix[i][matrix[0].length-1]; //the last values of each row
		}
			
		//obj function
		for (int j=0;j<matrix[0].length-1;j++) {
			table[table.length-1][j]= matrix[matrix.length-1][j]*-1;//fill last row with obj func
		}
		
		//printing initial table with slacks
		
		printInitialTable();
		
	}
	private void printInitialTable() {
		outputBuilder.append("This is the new initial table with slacks:\n");
		printMatrix();
	}
	
	 // Method to check if the solution is optimal(No negatives in obj row)
    private boolean isOptimal() {
        for (int j = 0; j < table[numberOfConstraints].length - 1; j++) {
            if (table[numberOfConstraints][j] < 0) {
                return false;//found a negative so we keep solving
            }
        }
        return true;
    }
    
    // Method to find the pivot column (Search for the smallest value in the obj row) 
    private int findPivotColumn() {
        int pivotCol = 0;
        for (int j = 1; j < table[numberOfConstraints].length - 1; j++) {
            if (table[numberOfConstraints][j] < table[numberOfConstraints][pivotCol]) {
                pivotCol = j;
            }
        }
        outputBuilder.append(String.format("%.2f",table[numberOfConstraints][pivotCol])+"<-- Pivot Column \n");//////
        return pivotCol;
    }
    
    // Method to find the pivot row
    private int findPivotRow(int pivotCol) {
        int pivotRow = -1;
        double minRatio = Double.POSITIVE_INFINITY;

        for (int i = 0; i < numberOfConstraints; i++) {
            double ratio = table[i][table[0].length - 1] / table[i][pivotCol];//dividing rhs by all in the pivot col
            if (ratio > 0 && ratio < minRatio) {
                minRatio = ratio;
                pivotRow = i;
            }
            outputBuilder.append(String.format("%.2f",table[i][table[0].length - 1])+"/"+String.format("%.2f",table[i][pivotCol])+" "+String.format("%.2f",ratio)+"<-- ratio #"+(i+1)+"\n");//////
        }
        outputBuilder.append(String.format("%.2f",minRatio)+"<-- minimum ratio \n");//////
        return pivotRow;
    }
    
    // Method to perform the pivot operation
    private void pivot(int row, int col) {
        // Divide pivot row by pivot element
        double pivotElement = table[row][col];
        outputBuilder.append("["+String.format("%.2f",pivotElement)+"]"+" <-This is the pivot from row-col: "+row+","+col);//////
        for (int j = 0; j < table[0].length; j++) {
            table[row][j] /= pivotElement;
        }

        // Subtract multiples of pivot row from other rows
        for (int i = 0; i < table.length; i++) {
            if (i != row) {
                double factor = table[i][col];
                for (int j = 0; j < table[0].length; j++) {
                    table[i][j] -= factor * table[row][j];
                }
            }
        }
        //Prints the current state of the matrix (table) after the pivot
        this.loopNum++;
        outputBuilder.append("\nThis is loop number: ").append(loopNum).append("\n");
        printMatrix();
  
    }
    //Prints the matrix state
    public void printMatrix() {
    	int slackNum=0;
        int columnWidth=8;
        
        //used as a way to put values into the outputbuilder
        
        //PRINTS EACH LOOP INSTANCE AFTER PIVOT OPERATION 
        if (isMinimization==true) {
        	
        	 //to count each slack without having to use another for loop to print mask
        	
        	//if j is less than the number of variables we print variables, else we print slacks, else if at the end we print rhs
         	for (int j=0;j<table[0].length;j++){
         		if (j<numberOfVariables) {
         			
         			outputBuilder.append(String.format("%-" + columnWidth + "s", "y" + (j + 1)));
         		} else if (j==table[0].length-1) {
         			outputBuilder.append(String.format("%-" + columnWidth + "s", "rhs"));
         			outputBuilder.append("\n");
         		}
         		else {
         			outputBuilder.append(String.format("%-" + columnWidth + "s", "s" + (++slackNum)));
         		}
         	}
             for (int i=0;i<table.length;i++) {
            	 for (int j=0;j<table[0].length;j++) {
            		 outputBuilder.append(String.format("%-" + columnWidth + ".2f", table[i][j]));
            	 }
            	 outputBuilder.append("\n");
             }
        } else {
            //print every loop instance if not a minimization problem
         	for (int j=0;j<table[0].length;j++){
         		//print variables mask
         		if (j<numberOfVariables) {
         			outputBuilder.append(String.format("%-" + columnWidth + "s", "x" + (j + 1)));
         		} else if (j==table[0].length-1) {
         			//print right hand sum mask
         			outputBuilder.append(String.format("%-" + columnWidth + "s", "rhs"));
         			outputBuilder.append("\n");
         		}
         		else {
         			//print slacks mask
         			outputBuilder.append(String.format("%-" + columnWidth + "s", "s" + (++slackNum)));
         		}
         	}
         	 for (int i=0;i<table.length;i++) {
            	 for (int j=0;j<table[0].length;j++) {
            		 outputBuilder.append(String.format("%-" + columnWidth + ".2f", table[i][j]));
            	 }
            	 outputBuilder.append("\n");
             }
        }  
    }
    //Returns the total output 
    public String getOutput() {
    	outputBuilder.append("\n");
    	return outputBuilder.toString();
    }
    
    public void solve() {
        while (!isOptimal()) {
            int pivotCol = findPivotColumn();
            int pivotRow = findPivotRow(pivotCol);

            if (pivotRow == -1) {
                throw new ArithmeticException("Unbounded solution");//handles problems with no solutions 
            }

            pivot(pivotRow, pivotCol);
        }
    }
    
    public double[] getSolution() {
        double[] solution = new double[numberOfVariables];
        for (int i = 0; i < numberOfConstraints; i++) {
            int basicVarIndex = -1;
            for (int j = 0; j < numberOfVariables; j++) {
                if (table[i][j] == 1.0) {
                    basicVarIndex = j;
                    break;
                }
            }
            if (basicVarIndex != -1) {
                solution[basicVarIndex] = table[i][table[0].length - 1];
            }  
        }
        //If it is a minimization problem we need to only show the values at the left of the last row in the right hand sum.
        if (isMinimization==true) {
        
        	double[] Altsolution = new double[numberOfVariablesOriginal];
        	for (int i=0;i<numberOfVariablesOriginal;i++) {
        		Altsolution[i]=table[numberOfConstraints][table[0].length-numberOfVariablesOriginal-1+i];//take the values from left to right from the last row
        	}
        	return Altsolution;
        }
      
        return solution;
    }
    
    public double getOptimalValue() {
        return table[numberOfConstraints][table[0].length - 1];
    }
}
