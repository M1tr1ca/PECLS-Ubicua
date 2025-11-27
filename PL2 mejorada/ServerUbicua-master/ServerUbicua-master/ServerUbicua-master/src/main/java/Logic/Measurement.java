package logic;

// sirve para LEER datos de la BD, NO para guardarlos.
import java.sql.Timestamp;

public class Measurement 
{
    private int value;
    private Timestamp date;
 
    // constructors
    public Measurement() 
    {
    	this.value = 0;
    	this.date = null;
    }

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}
    
 }
