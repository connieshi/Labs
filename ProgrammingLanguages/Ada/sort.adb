with Sort;
with text_io;
with Ada.Integer_Text_IO;

package body Sort is

	-- Merge Sort procedure that calls the helper procedure SortHalf
	procedure MergeSort(MyArray : in out Int_Array; Result : in out Int_Array) is
		use Sort;
		use text_io;
		use Ada.Integer_Text_IO;
	begin
		SortHalf(MyArray'First, MyArray'Last, MyArray, Result);
	end MergeSort;

	-- Splits the array into halves until base case, then merges elements in a sorted array into Result
	procedure SortHalf(L, H : in Integer; MyArray : in out Int_Array; Result : in out Int_Array) is
		
		-- Sort the left side of the array
		task Left_Sort is
			entry leftsort(L, M : Integer);
		end Left_Sort;

		-- Sort the right side of the array
		task Right_Sort is
			entry rightsort(M, H : Integer);
		end Right_Sort;

		-- Merge the two sorted halves
		task Merge is
			entry mergeArray(L, M, H : Integer);
			entry doneLeft;
			entry doneRight;
		end Merge;

		-- Accepts leftsort rendezvous and only sort if low < high
		task body Left_Sort is
			low : Integer;
			high : Integer;
		begin
			accept leftsort(L, M : Integer) do
				low := L;
				high := M;
			end leftsort;
			if low < high then
				SortHalf(low, high, MyArray, Result);
			end if;
			Merge.doneLeft; -- Merge should only run after the splitting steps are done
		end Left_Sort;

		-- Accepts rightsort rendezvous and only sort if low < high
		task body Right_Sort is
			low : Integer;
			high : Integer;
		begin
			accept rightsort(M, H : Integer) do
				low := M;
				high := H;
			end rightsort;
			if low < high then
				SortHalf(low, high, MyArray, Result);
			end if;
			Merge.doneRight; -- Merge should only run after splitting is done
		end Right_Sort;

		-- Merge two sorted arrays into Result array and overwrite the current array
		-- Add elements in order from low to mid and mid2 to high
		task body Merge is
			low : Integer;
			low_temp : Integer;
			mid : Integer;
			mid2 : Integer;
			high : Integer;
			temp_Array : Int_Array; -- Use temporary array to store merging result
			temp_index : Integer;
		begin
			accept mergeArray(L, M, H : Integer) do
				low := L;
				low_temp := L;
				mid := M;
				mid2 := M + 1;
				high := H;
				temp_index := L;
			end mergeArray;

			-- Make sure that left and right sort splittings are done before merging
			accept doneLeft;
			accept doneRight;

			-- While either low_temp nor mid2 have reached the end, keep comparing 
			-- their elements and adding to temporary array
			while low_temp <= mid AND mid2 <= high loop
				if MyArray(low_temp) <= MyArray(mid2) then
					temp_Array(temp_index) := MyArray(low_temp);
					temp_index := temp_index + 1;
					low_temp := low_temp + 1;
				else
					temp_Array(temp_index) := MyArray(mid2);
					temp_index := temp_index + 1;
					mid2 := mid2 + 1;
				end if;
			end loop;
	
			-- If one side is done, add all that's left of the other side
			while low_temp <= mid loop
				temp_Array(temp_index) := MyArray(low_temp);
				temp_index := temp_index + 1;
				low_temp := low_temp + 1;
			end loop;
			while mid2 <= high loop
				temp_Array(temp_index) := MyArray(mid2);
				temp_index := temp_index + 1;
				mid2 := mid2 + 1;
			end loop;
		
			-- Transfer result to the Result array and update MyArray to persist
			Result(low..high) := temp_Array(low..high);
			MyArray(low..high) := temp_Array(low..high);
	end Merge;			

	-- Set local variables in SortHalf procedure
	low : Integer := L;
	high : Integer := H;
	mid : Integer := (L + H) / 2;

	-- Body of SortHalf procedure
	begin
		Left_Sort.leftsort(low, mid);
		Right_Sort.rightsort(mid + 1, high);
		Merge.mergeArray(low, mid, high);
	end SortHalf;

end Sort;
