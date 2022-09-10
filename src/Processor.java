import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Processor {
	//instance variables 
	Memory m =new Memory();
	int PC ;
	Object [] Registers = new Object[32]; //R0 ---> R31
	int clockcycles; //Timer 
	boolean branchTaken = false;
	
	//Pipeline Registers 
	
	 String IfIdwrite = "";
	 String IfIdRead = "";
	 String IdExWrite = "";
	 String IdExRead = "";
	 String ExMemWrite = "";
	 String ExMemRead = "";
	 String MemWB = "";
	
	//Controls of Control Unit 
	 
	 
	//constructor
	 public Processor () {
		 this.PC=0;
		 this.clockcycles=1;
		 this.Registers[0] = "0"; //hardwired 0 at R0
	 }
	
	
	//Methods
	
	 public double binaryToDecimal(String binary) {
			double decimal = 0;
			int j = 0;
			for(int i=binary.length()-1; i>=0; i--) {
			    if(Integer.parseInt(binary.charAt(i) + "") == 1){
			        decimal += Math.pow(2, j);
			    }
				j++;
			}
			return decimal;
		}
	
	 public static int SignedtoDecimal (String binary ) {   //0010 ---> +2 //1110 ---> -2
		 if (binary.charAt(0)=='0') {
			 return Integer.parseInt(binary,2);
		 }
		 else {
			 String flipped="" ;
			 for(int i=0;i<binary.length();i++) { //1001  0
				 if(binary.charAt(i)=='0') {
					 flipped =flipped +"1" ;
				 }
				 if(binary.charAt(i)=='1') {
					 flipped =flipped +"0" ;
				 }
				
			 }
		
			 int twosscomplement =Integer.parseInt(flipped,2) +1 ;
			 return twosscomplement * (-1)	;	
		 }
	 }
	
	public static String unsignedExtend (int finalLength,String binarystring) {
		while (binarystring.length()<finalLength) {
			binarystring ="0" +binarystring ;
		}
		return binarystring ;
	}
	
	public static String signedExtend (int finalLength,String binarystring) {
		String signbit =binarystring.charAt(0) +""; //10000111
		while (binarystring.length()<finalLength) {
			binarystring =signbit +binarystring ;
		}
		return binarystring ;
	}
	
	
	
	public static String[] Rtypeconverter(String [] parsed) {
		String [] converted=new String [5];
		converted[4] = "0000000000000";
		if(parsed[0].equals("ADD")) 
			converted[0]="0000";
		else if(parsed[0].equals("SUB"))
			converted[0]="0001";
		else if(parsed[0].equals("MUL"))
			converted[0]="0010";
		else if(parsed[0].equals("AND"))
			converted[0]="0101";
		else if(parsed[0].equals("LSL")) {  
			converted[0]="1000";
			converted[3]="00000";
			converted[4]=unsignedExtend(13,Integer.toBinaryString(Integer.parseInt(parsed[3]))); 
			}
		else if(parsed[0].equals("LSR")) {
			converted[0]="1001";
			converted[3]="00000";
			converted[4]= unsignedExtend(13,Integer.toBinaryString(Integer.parseInt(parsed[3]))); //"2"  , // taking last 13 bits  //0000000000000000000
		}
		//dest
		if(parsed[1].length()==2) //if no. of register is only 1 digit EX: R1
			converted[1]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[1].charAt(1)+"")));   // register number  is 5 bits 
		else if(parsed[1].length()==3) //if no. of register is only 2 digits Ex: R31
			converted[1]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[1].charAt(1)+""+parsed[1].charAt(2)+"")));
		//src 1
		if(parsed[2].length()==2) //if no. of register is only 1 digit EX: R1
			converted[2]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[2].charAt(1)+""))); 
		else if(parsed[2].length()==3) //if no. of register is only 2 digits Ex: R31
			converted[2]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[2].charAt(1)+""+parsed[2].charAt(2)+"")));
		//src 2
		if(!parsed[0].equals("LSR") && !parsed[0].equals("LSL") ) {
		if(parsed[3].length()==2) //if no. of register is only 1 digit EX: R1
			converted[3]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[3].charAt(1)+""))); 
		else if(parsed[2].length()==3) //if no. of register is only 2 digits Ex: R31
			converted[3]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[3].charAt(1)+""+parsed[3].charAt(2)+"")));
		}
		return converted;
	}
	

	public static String[] Itypeconverter(String[] parsed) {
		String binaryString = "";
		String [] converted=new String [4];
		converted[2] = "00000";
		if(parsed[0].equals("MOVI")) {
			converted[0]="0011";
			converted[3]=signedExtend(18,Integer.toBinaryString(Integer.parseInt(parsed[2])) ) ;
		}
		else if(parsed[0].equals("JEQ"))
			converted[0]="0100";
		else if(parsed[0].equals("XORI"))
			converted[0]="0110";
		else if(parsed[0].equals("MOVR"))
			converted[0]="1010";
		else if(parsed[0].equals("MOVM"))
			converted[0]="1011";
		
		//Registers 
		//dest
		if(parsed[1].length()==2) //if no. of register is only 1 digit EX: R1
			converted[1]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[1].charAt(1)+"")));   // register number  is 5 bits 
		else if(parsed[1].length()==3) //if no. of register is only 2 digits Ex: R31
			converted[1]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[1].charAt(1)+""+parsed[1].charAt(2)+"")));
		//src 1
		if(!parsed[0].equals("MOVI")) {
		if(parsed[2].length()==2) //if no. of register is only 1 digit EX: R1
			converted[2]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[2].charAt(1)+""))); 
		else if(parsed[2].length()==3) //if no. of register is only 2 digits Ex: R31
			converted[2]=unsignedExtend(5,Integer.toBinaryString(Integer.parseInt(parsed[2].charAt(1)+""+parsed[2].charAt(2)+"")));
		
		//Immediate
		if(Integer.parseInt(parsed[3]) > 0) {
			binaryString = "0" + Integer.toBinaryString(Integer.parseInt(parsed[3]));
			converted[3]=signedExtend(18, binaryString);
		}
		else {
			converted[3]=signedExtend(18, Integer.toBinaryString(Integer.parseInt(parsed[3])));
		}
		}
		else {
			if(Integer.parseInt(parsed[2]) > 0) {
				binaryString = "0" + Integer.toBinaryString(Integer.parseInt(parsed[2]));
				converted[3]=signedExtend(18, binaryString);
			}
			else {
				converted[3]=signedExtend(18, Integer.toBinaryString(Integer.parseInt(parsed[2])));
			}
		}
		
		return converted;	
	}
	
	public static String[] Jtypeconverter(String[] parsed) {
		String [] converted=new String [2];
		//put the opcode and address
		converted[0]="0111";
		converted[1]=unsignedExtend(28,Integer.toBinaryString(Integer.parseInt(parsed[1])));
		return converted;
	}
	

	public void parseAndLoadProgram (File program) throws IOException {
		Scanner sc = null;
		try {
			sc = new Scanner (program);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(sc.hasNextLine()) {
			String line =sc.nextLine();
			String []parsed =line.split(" ");
			
			if(parsed.length == 4 && (parsed[0].equals("ADD") || parsed[0].equals("SUB") 
					|| parsed[0].equals("MUL") || parsed[0].equals("LSL") || parsed[0].equals("LSR")
					|| parsed[0].equals("AND"))) {
				String[] converted = Rtypeconverter(parsed);
				String Instruction ="";
				for (int i=0;i<converted.length;i++) {
					Instruction =Instruction + converted[i];
				}
				m.insertInstruction (Instruction);	
			}
			else if(parsed.length == 4 || parsed.length == 3) {
				String[] converted = Itypeconverter(parsed);
				String Instruction ="";
				for (int i=0;i<converted.length;i++) {
					Instruction =Instruction + converted[i];
				}
				m.insertInstruction (Instruction);
				// add converted to memory
			}
			else if(parsed.length==2) {
				String[] converted = Jtypeconverter(parsed);
				String Instruction ="";
				for (int i=0;i<converted.length;i++) {
					Instruction =Instruction + converted[i];
				}
				m.insertInstruction (Instruction);
			}
		}
	}
	
	//STAGES Methods 
	
	//FETCH 
	public  void fetch () {
		if((this.m.memory[this.PC]) != null) {
			IfIdwrite=(String)(this.m.memory[this.PC]) + "#" + this.PC;
		}
		else {
			IfIdwrite = "";
		}
		this.PC++;
	}
	
	//DECODE 
	public void decode (String IfIdRead) {
		if (IfIdRead.equals(""))
			return; 
	
		String inst[] = IfIdRead.split("#");
		
		int opcode = 0;  
		int R1 = 0; //dest     
		int R2 = 0; //src1  
		int R3 = 0; //src2   
		int shamt = 0;   
		int imm = 0;     
		int address = 0; 
        
        int valueR2 = 0;
        int valueR3 = 0;
        int valueR1 = 0;
        
        opcode = (int) binaryToDecimal(inst[0].substring(0,4));
        R1 = (int) binaryToDecimal(inst[0].substring(4,9));
        R2 = (int) binaryToDecimal(inst[0].substring(9,14));
        R3 = (int) binaryToDecimal(inst[0].substring(14,19));
        shamt = (int) binaryToDecimal(inst[0].substring(19));
        imm = SignedtoDecimal(inst[0].substring(14));
        address = (int) binaryToDecimal(inst[0].substring(4));
        
        if(Registers[R2] != null) {
        	valueR2 = Integer.parseInt((String) Registers[R2]);
        }
        else {
        	valueR2 = 0;
        }
        
        if(Registers[R1] != null) {
        	valueR1 = Integer.parseInt((String) Registers[R1]);
        }
        else {
        	valueR1 = 0;
        }
        
        if(Registers[R3] != null) {
        	valueR3 = Integer.parseInt((String) Registers[R3]);
        }
        else {
        	valueR3 = 0;
        }
        
        IdExWrite = opcode + "#" + R1 + "#" + shamt + "#" + imm + "#" + address + "#" + valueR2 + "#" + valueR3 + "#" + valueR1 + "#" + inst[1];
	}
	
	//EXECUTE
	public  void execute  (String IdExRead) {
		if (IdExRead.equals(""))
			return;
		String[] pReg = IdExRead.split("#");
		String ExMemEnter = "";
		String writeBack = "X"; //immediate
		String memaddress = "X";
		String controlmem = "X";
		String controlwb = "0";
		String controlwbchoose = "0"; // 0 = wb alu output, 1 = wbmemread
		
		if(pReg[0].equals("0")) {
			int temp = Integer.parseInt(pReg[5]) + Integer.parseInt(pReg[6]);
			writeBack = temp + "";
			controlwb = "1";
	}
		else if(pReg[0].equals("1")) {
			int temp = Integer.parseInt(pReg[5]) - Integer.parseInt(pReg[6]);
			writeBack = temp + "";
			controlwb = "1";
	}
		else if(pReg[0].equals("2")) {
			int temp = Integer.parseInt(pReg[5]) * Integer.parseInt(pReg[6]);
			writeBack = temp + "";
			controlwb = "1";
	}
		else if(pReg[0].equals("3")) {
			writeBack = pReg[3];
			controlwb = "1";
	}
		else if(pReg[0].equals("4")) {
			if(Integer.parseInt(pReg[5]) == Integer.parseInt(pReg[7])) {
				this.PC =  Integer.parseInt(pReg[8]) + 1 + Integer.parseInt(pReg[3]);
				branchTaken = true;
			}
				
		}
		else if(pReg[0].equals("5")) {
			int temp = Integer.parseInt(pReg[5]) & Integer.parseInt(pReg[6]);
			writeBack = temp + "";
			controlwb = "1";
		}
		else if(pReg[0].equals("6")) {
			int temp = Integer.parseInt(pReg[5]) ^ Integer.parseInt(pReg[3]);
			writeBack = temp + "";
			controlwb = "1";
		}
		
		else if(pReg[0].equals("7")) {
			this.PC = (int) binaryToDecimal((unsignedExtend(32,Integer.toBinaryString(this.PC)).substring(0,4)+ Integer.toBinaryString(Integer.parseInt(pReg[4]))));
			branchTaken = true;
		}
		else if(pReg[0].equals("8")) {
			writeBack = (Integer.parseInt(pReg[5]) << Integer.parseInt(pReg[2])) + "";
			controlwb = "1";
		}
		else if(pReg[0].equals("9")) {
			writeBack = (Integer.parseInt(pReg[5]) >> Integer.parseInt(pReg[2])) + "";
			controlwb = "1";
		}
		else if(pReg[0].equals("10")) {
			int address = Integer.parseInt(pReg[5]) + Integer.parseInt(pReg[3]);
			memaddress = address + "";
			controlwbchoose = "1";
			controlmem = "1";
			controlwb = "1";
		}
		else if(pReg[0].equals("11")) {
			int address = Integer.parseInt(pReg[5]) + Integer.parseInt(pReg[3]);
			memaddress = address + "";
			controlmem = "0";
		}
		
		ExMemEnter += writeBack + "#" + pReg[1] + "#" + memaddress + "#" + controlmem + "#" + pReg[7] + "#" + controlwb + "#" + controlwbchoose + "#" + pReg[8];
		ExMemWrite = ExMemEnter;
		IdExRead = "";
	}
	
    //MEM

		public void mem(String exMemRead) {
			if(!exMemRead.equals("")) {
				String [] MemWBAr = exMemRead.split("#");
				String wb = "";
				wb += MemWBAr[0] + "#"; //immediate
				wb += MemWBAr[1]; //destreg
				
				if(!MemWBAr[3].equals("X")) {
				
					if(MemWBAr[3].equals("1")) {
						Object read = m.memory[Integer.parseInt(MemWBAr[2])];
						wb += "#" + read; //memread
					}
					else {
						m.insertData(MemWBAr[4], Integer.parseInt(MemWBAr[2]));
						wb += "#" + "X"; //memwrite
					}
					
				}
				else {
					wb +="#X"; //mem if no read or write
				}
				wb +=  "#" + MemWBAr[5]+"#"+ MemWBAr[6] + "#" + MemWBAr[7]; //controlwb, controlwbchoose, PC
				MemWB = wb;
				exMemRead = "";
			}
			else {
				return;
			}
		}
		
		//WB

		public void wb(String memWBRead) {
			if(!memWBRead.equals("")) {
				String [] WBFinal = memWBRead.split("#");
				if(!WBFinal[3].equals("0")) { //checking controlwb
					if(WBFinal[4].equals("1")) { //checking controlwbchoose
						if(!WBFinal[1].equals("0")) { //making sure its not writing into R0
							Registers[Integer.parseInt(WBFinal[1])] = WBFinal[2];
						}
					}
					else {
						if(!WBFinal[1].equals("0")) {
							Registers[Integer.parseInt(WBFinal[1])] = WBFinal[0];
						}
					}
					memWBRead = "";
				}
			}
			else {
				return;
			}
		}
	
	//Test 
	
		public static void main (String [] args) throws IOException {
			Processor p=new Processor();
			File program =new File ("Program.txt");
			p.parseAndLoadProgram(program);
			
			//parsing the program 
			while ((p.PC-2)<=p.m.instructionpointer) { //not pc-4 because it fetches every 2 cycles not every cycle
				if(p.clockcycles%2==1) { //odd clock cycles
					p.fetch();
					p.wb(p.MemWB);
					if(p.branchTaken == true) {
				    	p.IfIdRead = "";
				    	p.IdExWrite = "";
				    	p.branchTaken = false;
				    }
					
					p.ExMemRead=p.ExMemWrite;
					p.ExMemWrite = "";
					
					//Printings of odd clock cycles
					
					//printing clock cycle no
					System.out.println("Clock cycle: "+p.clockcycles);
					//printing  Which instruction is being executed at each stage
					
					if(!p.IfIdwrite.equals("")){//null means there is no instruction
						String[] findPC = p.IfIdwrite.split("#");
						System.out.println("Fetch stage has Instruction number " + findPC[1]);//charAt(32) gives us the PC value when this instruction is fetched if it is the first instruction then PC=0
					}
					else {
						System.out.println("Fetch stage is not active");
					}
					if(!p.IfIdRead.equals("")) {
						String[] parameters = p.IfIdRead.split("#");
						System.out.println("Decode stage has Instruction number "+ parameters[1]); //"" where the value of PC is located in this pipeline register 
						System.out.println("Input parameters ---> Instruction: " + parameters[0] + ", PC: " + parameters[1]);
						}
					else {
						System.out.println("Decode stage is not active");
					}
					if(!p.IdExRead.equals("")){
						String[] parameters = p.IdExRead.split("#");
						System.out.println("Execute stage has Instruction number "+ parameters[8]);//"" where the value of PC is located in this pipeline register
						System.out.println("Input parameters ---> Opcode: " + parameters[0] + ", DestReg: R" + parameters[1] + ", Shamt: "
								+ parameters[2] + ", Immediate: " + parameters[3] + ", Address: "
								+ parameters[4] + ", ValueRS: " + parameters[5] + ", ValueRT: " + parameters[6] + ", ValueR1: "
								+ parameters[7] + ", PC: " + parameters[8]);
					}
					else {
						System.out.println("Execute stage is not active");
					}
					
					System.out.println("Memory stage is not active");
					
					if(!p.MemWB.equals("")) {
						String[] parameters = p.MemWB.split("#");
						System.out.println("Writeback Stage has Instruction number " + parameters[5]);//"" where the value of PC is located in this pipeline register
						System.out.println("Input parameters ---> Immediate: " + parameters[0] + ", DestReg: R"
								+ parameters[1] + ", MemoryRead Data: " + parameters[2] + ", WB Control: "
								+ parameters[3] + ", WB Type Control: " + parameters[4] + ", PC: " + parameters[5]);
						if(parameters[3].equals("1") && !parameters[1].equals("0")) {
							System.out.println("Register Updates ---> R" + parameters[1] + ": " + p.Registers[Integer.parseInt(parameters[1])]);
						}
						p.MemWB = "";
					}
					else {
						System.out.println("WB stage is not active");
					}
					System.out.println();
					System.out.println();
				} //end of odd cycle if
				
				else {
					p.IfIdRead=p.IfIdwrite;
				
					p.IdExRead=p.IdExWrite;
					p.IdExWrite = "";
					
					p.decode(p.IfIdRead);
				
				    p.execute(p.IdExRead);
				    
				    p.mem(p.ExMemRead);
				    
				    //Printing of even cycles 
				    //printing clock cycle no
					System.out.println("Clock cycle: "+p.clockcycles);
					
					//printing  Which instruction is being executed at each stage
					
					System.out.println("Fetch stage is not active");
					
					if(!p.IfIdRead.equals("")) {
						String[] parameters = p.IfIdRead.split("#");
						System.out.println("Decode stage has Instruction number "+ parameters[1]); //"" where the value of PC is located in this pipeline register 
						System.out.println("Input parameters ---> Instruction: " + parameters[0] + ", PC: " + parameters[1]);
						}
					else {
						System.out.println("Decode stage is not active");
					}
					if(!p.IdExRead.equals("")) {
						String[] parameters = p.IdExRead.split("#");
						System.out.println("Execute stage has Instruction number "+ parameters[8]);
						System.out.println("Input parameters ---> Opcode: " + parameters[0] + ", DestReg: R" + parameters[1] + ", Shamt: "
								+ parameters[2] + ", Immediate: " + parameters[3] + ", Address: "
								+ parameters[4] + ", ValueRS: " + parameters[5] + ", ValueRT: " + parameters[6] + ", ValueRD: "
								+ parameters[7] + ", PC: " + parameters[8]);
					}
					else {
						System.out.println("Execute stage is not active");
					}
					
					if(!p.ExMemRead.equals("")) {
						String[] parameters = p.ExMemRead.split("#");
						System.out.println("Memory stage has Instruction number " + parameters[7]);
						System.out.println("Input parameters ---> Immediate: " + parameters[0] + ", DestReg: R"
								+ parameters[1] + ", Address: " + parameters[2] + ", MEM Control: "
								+ parameters[3] + ", ValueRD: " + parameters[4] + ", WB Control: " 
								+ parameters[5] + ", WB Type Control: " + parameters[6] + ", PC: " + parameters[7]);
						if(parameters[3].equals("0")) {
							System.out.println("Memory Updates ---> Address " + parameters[2] + ": " + p.m.memory[Integer.parseInt(parameters[2])]);
						}	
					}
					else {
						System.out.println("Memory stage is not active");
					}
					System.out.println("WB stage is not active");
					//fetch stage 
					System.out.println();
					System.out.println();
				}
				p.clockcycles++;
			} //end of while loop
			
			System.out.print("Register File: [");
			for(int i = 0; i<p.Registers.length; i++) {
				if(p.Registers[i] != null) {
					if(i != (p.Registers.length-1)){
						System.out.print("R"+i+": " +p.Registers[i] + ", ");
					}
					else {
						System.out.print("R"+i+": " +p.Registers[i]);
					}
				}
			}
			System.out.println("PC: " + p.PC + "]");
			
			System.out.print("Memory: [");
			for(int i = 0; i<p.m.memory.length; i++) {
				if(p.m.memory[i] != null) {
					if(i != (p.m.memory.length-1)){
						System.out.print("Memory Address " + i + ": " + p.m.memory[i] + ", ");
					}
					else {
						System.out.print("Memory Address " + i + ": " + p.m.memory[i]);
					}
				}
			}
			System.out.println("]");
		}
}
