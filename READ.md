1. Approach is develop using TDD
2. Follow very good design pattern.
3. try as much java functional programming
4. welcome to the Match-3 game!. You will create a console app for a falling bricks game. In this game, you will control “bricks” made up of three blocks, each marked with a special symbol. Bricks can be horizontal or vertical, and you can move or drop them using simple commands. The goal is to place all bricks and clear as many matches as possible.
5. Initialization:
> pleas eneter field size(width and height) and upto 5 bricks set:
> allowed symbol: '~','^','*','@'
> up to 5 bricks can be provided
6 game loop
(a) the first bricks appears at the top of the field:
horizontal- centerd in the top row
vertical- centered in the top 3 rows
if the starting position is blocked, the game endsimediately
player can enter commands to move or drop the brick:

(b) each frame:
> dispaly the current field and active bricks.
> promot the user:
enter upto  2 commands to process before moving to the next frame(Valid commands L,R, D)
- only the first two valid commands are processed per frame.
- commmnds:
L- move left
R- move right
D- drop (move down until it hits another brick or the bottom of the field)
> after processing commands, the active brick moves down one row automatically.
> if the active brick cannot move down (it hits another brick or the bottom of the field), it becomes part of the field, and a new brick appears at the top.
> after placing a brick, check for matches:
- a match is defined as three or more consecutive
blocks of the same symbol in a row or column.
- if matches are found, remove them from the field and donot alllow blocks above fall down to fill the gaps. gravity is not applied.
> the next bricks appears at the top of the field.
> the game continues until a new brick cannot be placed at its starting position because it is  blocked by existing blocks.
7. End game:
> After the gameends, prompt the user to play again or exit.
- Enter S to start over or Q to quit
- Display a thank you message upon exiting.
  -thank you for playing Match-3 Falling Bricks Game!
