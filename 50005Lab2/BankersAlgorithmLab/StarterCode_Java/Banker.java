import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Banker {
	private int numberOfCustomers;	// the number of customers
	private int numberOfResources;	// the number of resources

	private int[] available; 	// the available amount of each resource
	private int[][] maximum; 	// the maximum demand of each customer
	private int[][] allocation;	// the amount currently allocated
	private int[][] need;		// the remaining needs of each customer

	/**
	* Constructor for the Banker class.
	* @param resources          An array of the available count for each resource.
	* @param numberOfCustomers  The number of customers.
	*/
	public Banker (int[] resources, int numberOfCustomers) {
		// TODO: set the number of resources
		numberOfResources = resources.length;

		// TODO: set the number of customers
		this.numberOfCustomers = numberOfCustomers;

		// TODO: set the value of bank resources to available
		available = resources.clone();

		// TODO: set the array size for maximum, allocation, and need
		maximum = new int[numberOfCustomers][numberOfResources];
		allocation = new int[numberOfCustomers][numberOfResources];
		need = new int[numberOfCustomers][numberOfResources];
	}

	/**
	* Sets the maximum number of demand of each resource for a customer.
	* @param customerIndex  The customer's index (0-indexed).
	* @param maximumDemand  An array of the maximum demanded count for each resource.
	*/
	public void setMaximumDemand(int customerIndex, int[] maximumDemand) {
		// TODO: add customer, update maximum and need
		maximum[customerIndex] = maximumDemand.clone();
		for(int i = 0; i < numberOfCustomers; i++){
			for(int j = 0; j < numberOfResources; j++){
				need[i][j] = maximum[i][j] - allocation[i][j];
			}
		}
	}

	/**
	* Prints the current state of the bank.
	*/
	public void printState() {
		System.out.println("\nCurrent state:");
		// print available
		System.out.println("Available:");
		System.out.println(Arrays.toString(available));
		System.out.println("");

		// print maximum
		System.out.println("Maximum:");
		for (int[] aMaximum : maximum) {
			System.out.println(Arrays.toString(aMaximum));
		}
		System.out.println("");
		// print allocation
		System.out.println("Allocation:");
		for (int[] anAllocation : allocation) {
			System.out.println(Arrays.toString(anAllocation));
		}
		System.out.println("");
		// print need
		System.out.println("Need:");
		for (int[] aNeed : need) {
			System.out.println(Arrays.toString(aNeed));
		}
		System.out.println("");
	}

	/**
	* Requests resources for a customer loan.
	* If the request leave the bank in a safe state, it is carried out.
	* @param customerIndex  The customer's index (0-indexed).
	* @param request        An array of the requested count for each resource.
	* @return true if the requested resources can be loaned, else false.
	*/
	public synchronized boolean requestResources(int customerIndex, int[] request) {
		// TODO: print the request
		System.out.println("Customer " + customerIndex + " requesting");
		System.out.println(Arrays.toString(request));
		// TODO: check if request larger than need
		if(!compareArray(request,need[customerIndex])){
			return false;
		}
		// TODO: check if request larger than available
		if(!compareArray(request,available)){
			return false;
		}
		// TODO: check if the state is safe or not
		if(!checkSafe(customerIndex, request)){
			return false;
		}

		// TODO: if it is safe, allocate the resources to customer customerNumber
		for(int i = 0; i < numberOfResources; i++){
			need[customerIndex][i] -= request[i];
			available[i] -= request[i];
			allocation[customerIndex][i] += request[i];
		}

		return true;
	}

	/**
	* Releases resources borrowed by a customer. Assume release is valid for simplicity.
	* @param customerIndex  The customer's index (0-indexed).
	* @param release        An array of the release count for each resource.
	*/
	public synchronized void releaseResources(int customerIndex, int[] release) {
		// TODO: print the release
		System.out.println("Customer " + customerIndex + " releasing");
		System.out.println(Arrays.toString(release));

		// TODO: release the resources from customer customerNumber
		for(int i = 0; i < numberOfResources; i++){
			need[customerIndex][i] += release[i];
			available[i] += release[i];
			allocation[customerIndex][i] -= release[i];
		}
	}

	/**
	* Checks if the request will leave the bank in a safe state.
	* @param customerIndex  The customer's index (0-indexed).
	* @param request        An array of the requested count for each resource.
	* @return true if the requested resources will leave the bank in a
	*         safe state, else false
	*/
	private synchronized boolean checkSafe(int customerIndex, int[] request) {
		// TODO: check if the state is safe
		int[] t_avail = available.clone();
		int[][] t_need = copy2DArray(need);
		int[][] t_allocation = copy2DArray(allocation);


		for(int i = 0; i < numberOfResources; i++){
			t_avail[i] -= request[i];
			t_need[customerIndex][i] -= request[i];
			t_allocation[customerIndex][i] += request[i];
		}

		int[] work = t_avail.clone();
		boolean[] isFinished = new boolean[numberOfCustomers];
		Arrays.fill(isFinished, Boolean.FALSE);
		boolean isPossible = true;

		while(isPossible){
			isPossible = false;
			for(int i = 0; i < numberOfCustomers; i++){
				if(!isFinished[i] && compareArray(t_need[i],work)){
					isPossible = true;
					for(int j = 0; j < numberOfResources; j++){
						work[j] += t_allocation[i][j];
					}
					isFinished[i] = true;
				}
			}
		}

		return areAllTrue(isFinished);
	}



	public static boolean areAllTrue(boolean[] array)
	{
	    for(boolean b : array) if(!b) return false;
	    return true;
	}



	/**
	* Parses and runs the file simulating a series of resource request and releases.
	* Provided for your convenience.
	* @param filename  The name of the file.
	*/
	public static void runFile(String filename) {

		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filename));

			String line = null;
			String [] tokens = null;
			int [] resources = null;

			int n, m;

			try {
				n = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 1.");
				fileReader.close();
				return;
			}

			try {
				m = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 2.");
				fileReader.close();
				return;
			}

			try {
				tokens = fileReader.readLine().split(",")[1].split(" ");
				resources = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++)
				resources[i] = Integer.parseInt(tokens[i]);
			} catch (Exception e) {
				System.out.println("Error parsing resources on line 3.");
				fileReader.close();
				return;
			}

			Banker theBank = new Banker(resources, n);

			int lineNumber = 4;
			while ((line = fileReader.readLine()) != null) {
				tokens = line.split(",");
				if (tokens[0].equals("c")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
						resources[i] = Integer.parseInt(tokens[i]);
						theBank.setMaximumDemand(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("r")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
						resources[i] = Integer.parseInt(tokens[i]);
						theBank.requestResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("f")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
						resources[i] = Integer.parseInt(tokens[i]);
						theBank.releaseResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("p")) {
					theBank.printState();
				}
			}
			fileReader.close();
		} catch (IOException e) {
			System.out.println("Error opening: "+filename);
		}

	}

	/**
	* Main function
	* @param args  The command line arguments
	*/
	public static void main(String [] args) {
		if (args.length > 0) {
			runFile(args[0]);
		}
	}

	private static boolean compareArray(int[] a1, int[] a2){
		for(int i = 0; i < a1.length; i++){
			if(a1[i] > a2[i]){
				return false;
			}
		}
		return true;
	}

	private static int[][] copy2DArray(int[][] a1){
    	int[][] a2 = new int[a1.length][a1[0].length];

	    for(int i = 0; i < a1.length; i++){
	      a2[i] = a1[i].clone();
	    }

		return a2;
	}

}
