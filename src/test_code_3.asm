# This program runs a loop that subtracts 1 from 0 until it reaches -32768.

addi $t1, $zero, -32768     # Load the immediate value -32768 into register $t1
addi $s0, $zero, 1          # Load the immediate value 1 into register $s0
sw $t1, 0($sp)              # Store the value of register $t1 at the address in $sp
loop:                       # Label for the loop
sub $t0, $t0, $s0           # Subtract the value in $s0 from $t0 and store the result in $t0
sw $t0, 4($sp)              # Store the value of register $t0 at the address in $sp + 4
bne $t0, $t1, loop          # If the value in $t0 is not equal to the value in $t1, branch to loop