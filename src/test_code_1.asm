# This program tests the basic instructions
# (addi, add, sub, sll, srl, lw, sw, bne, j, jal, jr)
# of the MIPS architecture.

# Initializes registers $t1 and $t0 with values 20 and 5, respectively.
# Enters a loop (test1) where $t0 is doubled until it equals $t1.
# Copies $t0 to $s0 and jumps to test2.
# In test2, performs shift and arithmetic operations, then returns.
# In test3, saves several registers to the stack, loads values back,
# performs an addition, and saves the result.

addi $t1, $zero, 20      # $t1 = 20
addi $t0, $zero, 5       # $t0 = 5
test1:                   # Label: test1
add $t0, $t0, $t0        # $t0 = $t0 + $t0
bne $t0, $t1, test1      # If $t0 != $t1, branch to test1
add $s0, $t0, $zero      # $s0 = $t0
jal test2                # branch to test2, $ra = PC + 4
add $s5, $t1, $t0        # $s5 = $t1 + $t0
j test3                  # branch to test3
test2:                   # Label: test2
sll $s1, $t1, 2          # $s1 = $t1 << 2
sub $s2, $s1, $t0        # $s2 = $s1 - $t0
sll $s3, $s2, 3          # $s3 = $s2 << 3
srl $s4, $s3, 1          # $s4 = $s3 >> 1
jr $ra                   # return to the address in $ra
test3:                   # Label: test3
sw $s0, 0($sp)           # Save $s0 to 0($sp) in the stack
sw $s1, 4($sp)           # Save $s1 to 4($sp) in the stack
sw $s2, 8($sp)           # Save $s2 to 8($sp) in the stack
sw $s3, 12($sp)          # Save $s3 to 12($sp) in the stack
sw $s4, 16($sp)          # Save $s4 to 16($sp) in the stack
sw $s5, 20($sp)          # Save $s5 to 20($sp) in the stack
lw $t0, 12($sp)          # Load $t0 from 12($sp) in the stack
lw $t1, 4($sp)           # Load $t1 from 4($sp) in the stack
add $s6, $t1, $t0        # $s6 = $t1 + $t0
sw $s6, 24($sp)          # Save $s6 to 24($sp) in the stack