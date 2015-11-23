with Sort;
with text_io;

-- Main function of Merge Sort program
procedure ProgMain is
	use text_io;
	package int_io is new integer_io(integer);
	use int_io;
	use Sort;

-- Local variables in ProgMain procedure
UnsortedArray : Int_Array;
SortedArray : Int_Array;

	-- Reads integers from command line and stores in array
	task Reader is
		entry read;
		entry doneInRead;
	end Reader;

	-- Computes the sum of the input array
	task Sum is
		entry getSum(inputArray : Int_Array);
		entry nowPrintSum;
	end Sum;

	-- Print elements of input array
	task Printer is
		entry print(inputArray : Int_Array);
	end Printer;

	-- Takes in integers from the command line to store in UnsortedArray
	task body Reader is
		input : Integer;
	begin
		accept read;
		Put_Line("Enter numbers to be sorted."); 
		for index in 1..SIZE loop
			get(input);
			UnsortedArray(index) := input;
		end loop;
		accept doneInRead;
	end Reader;

	-- Compute the sum of every element in the array and print the sum
	task body Sum is
		myArray : Int_Array;
		total : Integer := 0;
	begin
		accept getSum(inputArray : Int_Array) do
			myArray := inputArray;
		end getSum;

		for index in 1..SIZE loop
			total := total + myArray(index);
		end loop;

		-- Only print the sum after the elements have finished printing in Printer
		accept nowPrintSum;
		put_line("");
		put("The sum is: ");
		put(total);
	end Sum;

	-- Prints all elements of the array
	task body Printer is
		myArray : Int_Array;
	begin
		accept print(inputArray : Int_Array) do
			myArray := inputArray;
		end print;

		-- Rendezvous with Sum to run these two tasks concurrently
		Sum.getSum(myArray);
		
		for index in 1..SIZE loop
			put(myArray(index));
			if index mod 10 = 0 then -- Pretty printing
				put_line("");
			end if;
		end loop;
		put("");

		-- Now print the sum already calculated by Sum task
		Sum.nowPrintSum;
	end Printer;

-- ProgMain body
begin
	Reader.read;
	Reader.doneInRead; -- Waits until all numbers are saved before sorting
	MergeSort(UnsortedArray, SortedArray);
	Printer.print(SortedArray);
end ProgMain;
