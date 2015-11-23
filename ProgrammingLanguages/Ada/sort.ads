package Sort is
	SIZE : constant := 40;
	subtype MyInteger is Integer range -500..500;
	type Int_Array is array(1..SIZE) of MyInteger;
	procedure MergeSort(MyArray : in out Int_Array; Result : in out Int_Array);
	procedure SortHalf(L, H : in Integer; MyArray : in out Int_Array; Result : in out Int_Array);
end Sort;
