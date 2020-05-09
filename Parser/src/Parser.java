import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	public static void main(String[] args) {
		
		
		ArrayList<String> data = new ArrayList<String>();
		ArrayList<String> dataWithoutBlanks = new ArrayList<String>();
		ArrayList<String> functions = new ArrayList<String>();
		List<String> constraints = new ArrayList<String>();
		List<String> dataWithoutLastLine = new ArrayList<String>();
		List<Integer> indexes = new ArrayList<Integer>();
		List<Double> b = new ArrayList<Double>();
		List<Double> c = new ArrayList<Double>();
		List<List<Double>> A = new ArrayList<List<Double>>();
		List<Integer> MinMax = new ArrayList<Integer>();
		String variable = "";
		String function = "";
		int maxIndex;
		List<Integer> Eqin = new ArrayList<Integer>();
		String inputFile;
		String outputFile;
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Input file :");
		inputFile = scan.nextLine();
		
		try {
			File f = new File(inputFile);
			FileReader freader = new FileReader(f);
			BufferedReader reader = new BufferedReader(freader);
			
			String line  = reader.readLine();
			data.add(line);
			while(line != null) {
				
				line = reader.readLine();
				data.add(line);
				
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		dataWithoutBlanks = removeBlanksAndEmptyLines(data);
		
		//����������� min/max
		int minMax = getMinMax(dataWithoutBlanks.get(0));
		MinMax.add(minMax);
		
		//������������� ��������� ��� ����������� ����� min/max/st
		functions = removeMinMaxSt(dataWithoutBlanks);
		
		//������ ��� ���������� ��� ����������
		if(checkProblemVariables(functions)) {
			variable = getLineVariable(functions.get(0));
		}
		else
			System.out.println("Problem with the variables. Use the same character at all lines");
		
		
		dataWithoutLastLine = functions.subList(0, functions.size()-1);
		
		//����������� �������� ������
		function = String.join(" ", dataWithoutLastLine);
		indexes = getVariableIndexes(function,variable);
		maxIndex = Collections.max(indexes);
		
		for(int j=0; j<dataWithoutLastLine.size(); j++)
			checkOperands(dataWithoutLastLine.get(j),variable);
			
		//����� �� ���� ������������
		constraints = dataWithoutLastLine.subList(1, dataWithoutLastLine.size());
		
		
		c = getFactors(dataWithoutLastLine.get(0),variable,maxIndex);
		
		System.out.println("C = "+c);
		
		System.out.println("A = ");
		for(String line: constraints){
			List<Double> linefactors = new ArrayList<Double>();
			linefactors = getFactors(line,variable,maxIndex);
			A.add(linefactors);
		}
		
		System.out.println(A);
		
		for(String line: constraints) {
			Eqin.add(checkAndGetSymbols(line)); 
		}
		
		System.out.println("Eqin = "+Eqin);
		
		
		for(String line: constraints) {
			b.add(checkAndGetB(line));
		}
		
		System.out.println("B = " +b);
		
		System.out.println("Output File :");
		outputFile = scan.nextLine();
		
		File fout = new File(outputFile);
		
		try {
			FileWriter writer = new FileWriter(fout);
			writeToFile(fout,writer,"MinMax : "+MinMax);
			writeToFile(fout,writer,"C :"+c);
			writeToFile(fout,writer,"A :"+A.get(0));
			for(int i =1;i<constraints.size();i++)
				writeToFile(fout,writer,A.get(i));
			writeToFile(fout,writer,"Eqin :"+Eqin);
			writeToFile(fout,writer,"b :"+b);
			
			writer.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		scan.close();
		
	}
	
	
	public static ArrayList<String> removeBlanksAndEmptyLines(ArrayList<String> data) {
		
		ArrayList<String> dWithoutBlanks = new ArrayList<String>();
		
		for(int i=0; i<data.size()-1;i++) {
			String a = data.get(i);
			a = a.replaceAll("\\s", ""); //�������� �����
			a = a.toLowerCase();
			dWithoutBlanks.add(a);
			
		}
		dWithoutBlanks.removeAll(Collections.singletonList("")); //�������� ����� �������
		
		//���������� ��� �������� �� set ��� �� ���������� ����� ������� �� �����������
		Set<String> dataSet = new LinkedHashSet<String>(dWithoutBlanks); 
		dWithoutBlanks.clear();
		dWithoutBlanks.addAll(dataSet);
		
		return dWithoutBlanks;
	}
	
	public static int getMinMax(String line) {
		
		if(line.startsWith("min"))
			return -1;
		else if(line.startsWith("max"))
			return 1;
		else {
			System.out.println("Problem with min/max. Check again.");
			System.exit(0);
			return 0;
		}
		
	}
	
	public static boolean checkSt(String line) {
		
		if(line.startsWith("st") || line.startsWith("s.t.") || line.startsWith("subject to"))
			return true;
		else {
			System.out.println("Problem with st/s.t./subject to. Check again.");
			System.exit(0);
			return false;
		}
		
	}
	
	public static ArrayList<String> removeMinMaxSt(ArrayList<String> data){
		
		String line = null;
		ArrayList<String> dFunctions = new ArrayList<String>();
		
		//������� ������ �������
		line = data.get(0);
		line = line.replaceAll("max", "").replaceAll("min", "");
		dFunctions.add(line);
		
		//������� �������� �������
		line = data.get(1);
		line = line.replaceAll("st", "").replaceAll("s.t.", "").replaceAll("subjectto","");
		dFunctions.add(line);
		
		for(int j =2; j<data.size();j++) {
			dFunctions.add(data.get(j));
		}
		
		return dFunctions;
		
	}
	public static String getLineVariable(String line) {
		
		String simplifiedLine = null;
		boolean mistake = false;
		simplifiedLine = line.replaceAll("\\W", "").replaceAll("\\d", "");//�������� ����������� ���������� ��� ���������� ��� ��� ����� ��������
		//������� ���������� (�� ������������� �� ��� ���� ���������)
		for(int j = 0 ; j<simplifiedLine.length();j++) {
			if(simplifiedLine.charAt(j) != simplifiedLine.charAt(0)) {
				mistake = true;
			}
		
		}
		
		if(mistake) {
			System.out.println("You do not use the same characters for variables in line: "+line);
			System.exit(0);
		}
		return String.valueOf(simplifiedLine.charAt(0));
	}
	
		
	
	public static boolean checkProblemVariables(ArrayList<String> functions) {
		
		String lastLine = null;
		//���������� set ���� �� ��������� �� �� ���������� ����� �����
		Set<String> variables = new HashSet<String>();
		for(int i=0; i<functions.size()-1; i++) {
			
			variables.add(getLineVariable(functions.get(i)));
		}
		//�� ��������������� � ����� ���������� ��� ���� ��������� ������ ��� �� �� set 
		//���� ���� ��������� ���� ���� ������ ��� ���� ��������� ��� ��������
		lastLine = functions.get(functions.size()-1);
		if(variables.contains(String.valueOf(lastLine.charAt(0)))){
			if(variables.size()==1)
				return true;
				
		}
		
		return false;
			
	}
	
	
	public static List<Integer> getVariableIndexes(String data,String variable) {
		
		List<Integer> index = new ArrayList<Integer>();
		Pattern p = Pattern.compile(variable+"[0-9]+|"+variable);//���������� ������� ��� ������ "���������+�������" � ���� ���������
		Matcher m = p.matcher(data);
		String part = "";
		
		
		while(m.find()) {
			part = m.group();
			part = part.replaceAll(variable, "");//�������� ��� ��������� ��� ���������� 
			//�� �� part ����� ����� ���� �������� ��� ������ ��������� ����� ������
			if(part.isEmpty()) {
				System.out.println("An index of a variable missing! Check again.");
				System.exit(0);
			}
			else
				index.add(Integer.parseInt(part));
			
			
		}
		
		
		return index;
		
	}
	
	public static void checkOperands(String line,String variable) {
		
		
		
		line = line.replaceAll("\\d", "").replaceAll("[<=>==]", "");//�������� ����������� ���������� ��� �������� ��� ��������� ���� [ ]
		//�������� ������� ��� ���� ����������, �� ��������
		if(line.contains("."))
			line = line.replaceAll("\\.", "");
		
		//�� �������� � ������ �� + � - ������� ��� ������� ��������� ��� �� ��� �� ����� ���������
		if(line.startsWith("-")||line.startsWith("+")) {
			if(line.charAt(1) != variable.charAt(0)) {
				System.out.println("Problem with operands. Check agian.");
				System.exit(0);
		
			}
			line = line.replaceFirst("[+-]", "");
		}
		
		
		//������� ����� ���� ���������� ��� �������. ����� ���� ��� ��������� ��� ����������
		//������ �� � ������������ ����� + � - ����� �� ����������� ����� �����
		for(int i = 1; i<line.length(); i++) {
			if(line.charAt(i) == variable.charAt(0)) {
				if((int)line.charAt(i-1) != 43 && (int)line.charAt(i-1) != 45) {
					System.out.println("Problem with operands. Check again.");
					System.exit(0);
				}
			}
		}
		
	}
	
	public static int checkAndGetSymbols(String line) {
		
		int symbol = 2;
		
		line = line.replaceAll("\\d", "").replaceAll("[+-.]", "");//�������� ����������� ���������� ��� �������� [ ]
		if(line.endsWith("<="))
			symbol =-1;
		else if(line.endsWith(">="))
			symbol =  1;
		else if(line.endsWith("="))
			symbol =  0;
		else if(symbol == 2){
			System.out.println("Problem with symbols(<= , = , >=). Check again.");
			System.exit(0);
		}
		
		return symbol;
		
		
	}
	
	public static Double checkAndGetB(String line) {
		
		String result ="";
		
		Pattern p = Pattern.compile("[-][0-9]+$|[+][0-9]+$|[0-9]+$");//���������� ������� : ������� ��� ����� ��� �������
		Matcher m = p.matcher(line);
		if(m.find()) {
		    result = m.group();
		}
		else {
			System.out.println("There are no numbers after one of these symbols(<=,>=,=). Check again.");
			System.exit(0);
		}
		
		return Double.parseDouble(result);	
		
	}
	
	
	public static List<Double> getFactors(String line,String variable,int maxIndex){
		
		List<Double> numbers = new ArrayList<Double>();
		String part = "";

		//for ����� ��� ������� ������ ���� �� ����� ��� ���� ������ ��������� �� ������ ��� �
		for(int i =1 ; i<= maxIndex; i++) {
			
			//���������� ������� ��� ���� ��� ������� ����������� ����������
			//-xj,+xj,-ixj, +ixj, ixj , xj, +- ��������� xj
			Pattern p = Pattern.compile("[-]"+variable+Integer.toString(i)+"| [+]"+variable+Integer.toString(i)+"|[-+][0-9]+"+variable+Integer.toString(i)+"|[0-9]+"+variable+Integer.toString(i)+"|"+variable+Integer.toString(i)+"|[+-][0-9]+[\\.\\,][0-9]+"+variable+Integer.toString(i));
			Matcher m = p.matcher(line);
				
			//�� ������� ������ ��� �� ������
			if(m.find()) {
				part = m.group();
				
				//����� 1 �� ������� +xj � x 
				if(part.equals("+"+variable+Integer.toString(i)) || part.equals(variable+Integer.toString(i))) {
					numbers.add(1.0);
				}
				//����� -1 �� ������� -xj
				else if(part.contentEquals("-"+variable+Integer.toString(i))) {
					numbers.add(-1.0);
				}
				//����� ��� ������ ��� ������� ��� part 
				else {
					String mpart = part.replaceAll(variable+Integer.toString(i), "");
					numbers.add(Double.parseDouble(mpart));
				}
			}
			//�� ��� ������� ������ ��� �� ������ �������� ��� �� xi ��� ������� ���� ���������
			//��� ����� 0
			else
				numbers.add(0.0);
						
		}
		
		return numbers;
	}
	
	
	public static void writeToFile(File file ,FileWriter writer, Object obj) {
		
		try {
			
			writer.write(obj.toString()+"\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
		
}


