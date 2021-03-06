// TwoStateData.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for two-state data
 *
 * @version $Id: TwoStates.java,v 1.5 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class TwoStates extends SimpleDataType
{
	// Get number of bases
	public int getNumStates()
	{
		return 2;
	}

	// Get state corresponding to character c
	public int getState(char c)
	{
		switch (c)
		{
 			case '0': 
				return 0;
			case '1': 
				return 1;
				
			case UNKNOWN_CHARACTER:
				return 2;

			default:
				return 2;
		}
	}

	/**
		* @retrun true if this state is an unknown state
		*/
	public boolean isUnknownState(int state) {
		return(state>=2);
	}

	// Get character corresponding to a given state
	public char getChar(int state)
	{
		switch (state)
		{
			case 0:
				return '0';
			case 1:
				return '1';

			case 2:
				return UNKNOWN_CHARACTER;

			default:
				return UNKNOWN_CHARACTER;
		}
	}

	// String describing the data type
	public String getDescription()
	{
		return "Two state";
	}

	// Get numerical code describing the data type
	public int getTypeID()
	{
		return 2;
	}
}

