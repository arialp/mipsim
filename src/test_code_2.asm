# This assembly program performs basic arithmetic and logical operations.
# It loads immediate values into registers, performs bitwise AND and OR operations,
# compares values, and stores the results in memory.

# AND 170 and 204, store result in memory
addi $t0, $zero, 170  # Load immediate value 170 into register $t0
addi $t1, $zero, 204  # Load immediate value 204 into register $t1
and $t2, $t0, $t1     # Perform bitwise AND on $t0 and $t1, store result in $t2
sw $t2, 0($sp)        # Store word from $t2 into memory at address 0 offset from $sp

# OR 170 and 204, store result in memory
or $t3, $t0, $t1      # Perform bitwise OR on $t0 and $t1, store result in $t3
sw $t3, 4($sp)        # Store word from $t3 into memory at address 4 offset from $sp

# Loop adding 2+8 until 20 is reached, store result in memory
addi $t4, $zero, 8    # Load immediate value 8 into register $t4
addi $t5, $zero, 20   # Load immediate value 20 into register $t5
loop:                 # Label: loop
addi $t4, $t4, 2      # Add immediate value 2 to $t4
slt $t6, $t5, $t4     # Set $t6 to 1 if $t5 is less than $t4, otherwise set to 0
beq $zero, $t6, loop  # If $t5 is not less than $t4, loop
sw $t6, 8($sp)        # Store word from $t6 into memory at address 8 offset from $sp