procedure TEST_ADA is

   I, J : INTEGER;
   LoopVar, LoopVar2 : INTEGER;

procedure p(X : INTEGER) is
begin

    I := 1;
    LoopVar := 10;

    while I <= LoopVar loop
         I := I +1;
    end loop;

end p;

procedure A(X : INTEGER) is
begin
	
   exit when (X > 20);

end A;

begin
P(I);
A(J);
end TEST_ADA;