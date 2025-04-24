import numpy as np
import pandas as pd

'''
ask user to perform actions and then retrain the model
'''
def collect_user_input():
    return

def chat_with_user():
    game_to_play =print('Hi User! What game do you want to play? Press the RHS controller pad to play Tic Tac Toe, and the LHS controller pad to play Mario')

    if game_to_play == 'c':
        print('''You selected "Tic Tac Toe". Awesome!'\
        'For this game, you need to remap the keyboard controls to the controller' \
        'There are 5 different directions that you can move in chess. 
        To calibrate the controller, enter the mapping that feels most appropriate for each action.''')

        print('Conduct the action that you will do to move BACK on the board. Start the motion when it is indicated')
        print('Conduct the action that you will do to move FORWARD on the board. Start the motion when it is indicated')
        print('Conduct the action that you will do to move LEFT on the board. Start the motion when it is indicated')
        print('Conduct the action that you will do to move RIGHT on the board. Start the motion when it is indicated')
        print('Conduct the action that you will do to move DIAGONAL on the board. Start the motion when it is indicated')
        print()

    elif game_to_play == 'm':
        print('''You selected "Mario". Awesome!'\
        'For this game, you need to remap the keyboard controls to the controller' \
        'There are 5 different motions to conduct in Mario. 
        To calibrate the controller, enter the mapping that feels most appropriate for each action.''')

        print('Conduct the action that you will do to move RIGHT. Start the motion when it is indicated')
        print('Conduct the action that you will do to move LEFT. Start the motion when it is indicated')
        print('Conduct the action that you will do to JUMP UP. Start the motion when it is indicated')
        print('Conduct the action that you will do to JUMP RIGHT. Start the motion when it is indicated')
        print('Conduct the action that you will do to JUMP LEFT. Start the motion when it is indicated')
        print('To increase acceleration, press down more as you move. Test this functionality now')

    else:
        print('It seems you are done playing games. Bye!')
        