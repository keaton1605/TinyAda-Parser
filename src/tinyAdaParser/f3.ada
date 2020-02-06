procedure TEST_ADA is

   I : INTEGER;

procedure p(X : INTEGER) is

	J : INTEGER;
	B : INTEGER;
	
    begin
   
    I := 3 - 2;
    J := I + 4;
    X := J * I;
    
    if ( X - 3 >= 1 ) then
    	B := 1;
    	if (J <= 1 or I > 23) then
    		J := 32;
    	elsif (J < 1 and I /= 100) then
    		J := 0;
    	end if;
    else 
    	B := 0;
    elsif (J < 23) then
    	J := 2;
    end if; 

   end P;

begin
p(I);
end;
