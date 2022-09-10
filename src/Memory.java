
public class Memory {
	Object [] memory = new Object [2048] ;
	int instructionpointer = 0;

	public void insertInstruction (Object block) {
		memory[instructionpointer]=block;
		instructionpointer++;
	}

	public void insertData(Object Data ,int index) {
		if(index>2048 || index<1024) {
			System.out.println("Cannot insert :Invalid Data Address");
		}
		else 
			this.memory[index]=Data;
	}
}


