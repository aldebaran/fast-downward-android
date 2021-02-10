begin_version
3
end_version
begin_metric
0
end_metric
21
begin_variable
var0
-1
2
Atom was_attracted(human5)
NegatedAtom was_attracted(human5)
end_variable
begin_variable
var1
-1
2
Atom was_attracted(human4)
NegatedAtom was_attracted(human4)
end_variable
begin_variable
var2
-1
2
Atom was_attracted(human3)
NegatedAtom was_attracted(human3)
end_variable
begin_variable
var3
-1
2
Atom was_attracted(human2)
NegatedAtom was_attracted(human2)
end_variable
begin_variable
var4
-1
2
Atom was_attracted(human1)
NegatedAtom was_attracted(human1)
end_variable
begin_variable
var5
-1
2
Atom can_be_engaged(human5)
NegatedAtom can_be_engaged(human5)
end_variable
begin_variable
var6
-1
2
Atom can_be_engaged(human4)
NegatedAtom can_be_engaged(human4)
end_variable
begin_variable
var7
-1
2
Atom can_be_engaged(human3)
NegatedAtom can_be_engaged(human3)
end_variable
begin_variable
var8
-1
2
Atom can_be_engaged(human2)
NegatedAtom can_be_engaged(human2)
end_variable
begin_variable
var9
-1
2
Atom can_be_engaged(human1)
NegatedAtom can_be_engaged(human1)
end_variable
begin_variable
var10
-1
2
Atom engaged_with(human1)
NegatedAtom engaged_with(human1)
end_variable
begin_variable
var11
-1
2
Atom engaged_with(human2)
NegatedAtom engaged_with(human2)
end_variable
begin_variable
var12
-1
2
Atom engaged_with(human3)
NegatedAtom engaged_with(human3)
end_variable
begin_variable
var13
-1
2
Atom engaged_with(human4)
NegatedAtom engaged_with(human4)
end_variable
begin_variable
var14
-1
2
Atom engaged_with(human5)
NegatedAtom engaged_with(human5)
end_variable
begin_variable
var15
0
2
Atom new-axiom@0()
NegatedAtom new-axiom@0()
end_variable
begin_variable
var16
-1
2
Atom was_greeted(human1)
NegatedAtom was_greeted(human1)
end_variable
begin_variable
var17
-1
2
Atom was_checked_in(human1)
NegatedAtom was_checked_in(human1)
end_variable
begin_variable
var18
-1
2
Atom knows_intents(human1)
NegatedAtom knows_intents(human1)
end_variable
begin_variable
var19
0
2
Atom new-axiom@1()
NegatedAtom new-axiom@1()
end_variable
begin_variable
var20
0
2
Atom new-axiom@2()
NegatedAtom new-axiom@2()
end_variable
0
begin_state
1
1
1
1
1
1
1
1
1
1
1
1
1
1
1
0
1
1
1
0
1
end_state
begin_goal
1
20 0
end_goal
23
begin_operator
attract human1
0
2
0 9 -1 0
0 4 1 0
1
end_operator
begin_operator
attract human2
0
2
0 8 -1 0
0 3 1 0
1
end_operator
begin_operator
attract human3
0
2
0 7 -1 0
0 2 1 0
1
end_operator
begin_operator
attract human4
0
2
0 6 -1 0
0 1 1 0
1
end_operator
begin_operator
attract human5
0
2
0 5 -1 0
0 0 1 0
1
end_operator
begin_operator
disengage human1
0
1
0 10 0 1
1
end_operator
begin_operator
disengage human2
0
1
0 11 0 1
1
end_operator
begin_operator
disengage human3
0
1
0 12 0 1
1
end_operator
begin_operator
disengage human4
0
1
0 13 0 1
1
end_operator
begin_operator
disengage human5
0
1
0 14 0 1
1
end_operator
begin_operator
engage human1
2
9 0
15 1
1
0 10 -1 0
1
end_operator
begin_operator
engage human2
2
8 0
15 1
1
0 11 -1 0
1
end_operator
begin_operator
engage human3
2
7 0
15 1
1
0 12 -1 0
1
end_operator
begin_operator
engage human4
2
6 0
15 1
1
0 13 -1 0
1
end_operator
begin_operator
engage human5
2
5 0
15 1
1
0 14 -1 0
1
end_operator
begin_operator
goodbye human1
0
1
0 10 -1 1
1
end_operator
begin_operator
goodbye human2
0
1
0 11 -1 1
1
end_operator
begin_operator
goodbye human3
0
1
0 12 -1 1
1
end_operator
begin_operator
goodbye human4
0
1
0 13 -1 1
1
end_operator
begin_operator
goodbye human5
0
1
0 14 -1 1
1
end_operator
begin_operator
greet human1
1
10 0
1
0 16 -1 0
1
end_operator
begin_operator
scan human1
2
10 0
16 0
1
0 17 1 0
1
end_operator
begin_operator
show_menu human1
2
10 0
17 0
1
0 18 -1 0
1
end_operator
3
begin_rule
5
10 1
11 1
12 1
13 1
14 1
15 0 1
end_rule
begin_rule
1
18 0
19 0 1
end_rule
begin_rule
1
19 1
20 1 0
end_rule
