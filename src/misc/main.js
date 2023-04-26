const fs = require('fs');
const { createCanvas, loadImage, registerFont } = require('canvas');
registerFont('Oswald-Regular.ttf', { family: 'Oswald Regular' });
registerFont('Military Poster.ttf', { family: 'Military Poster Regular' });
const canvas = createCanvas(688, 276);
const ctx = canvas.getContext('2d')
const colorsRanks = {
  'Noob': "#ffffff",
  'Lucker': "#00ce21",
  'Strawberry Legend': "#ff8413",
  'Drunken Master': "#13f1ff",
  'Rambo': "#009fd1",
  'Terminator': "#2c5aff",
  'Terminator 2': "#c8ff2c",
  'Professional': "#a513ff",
  'Legend': "#ffd600",
  'Nanaxer': "#d00a0a",
};

function makeCtxFirstColumn() {
  ctx.fillStyle = '#d00a0a'
  ctx.globalAlpha = '1';
}

function makeCtxSecondColumn() {
  ctx.fillStyle = 'white'
  ctx.globalAlpha = '1';
}



loadImage('nanax_logo.png').then((image) => {
  ctx.fillStyle = 'black'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.globalAlpha = '0.2';
  ctx.drawImage(image, 50, 0, image.naturalWidth, image.naturalHeight)
  ctx.globalAlpha = '1';
  ctx.font = '28px Oswald';
  ctx.fillStyle = 'white'



  makeCtxSecondColumn();
  ctx.fillText('Country', 230, 55)
  makeCtxFirstColumn();
  ctx.fillText('Switzerland', 326, 55)

  makeCtxSecondColumn();
  ctx.fillText('Tag', 230, 90)
  makeCtxFirstColumn();
  ctx.fillText('Navi', 326, 90)

  makeCtxSecondColumn();
  ctx.fillText('Wins', 490, 55)
  makeCtxFirstColumn();
  ctx.fillText('134534', 572, 55)

  makeCtxSecondColumn();
  ctx.fillText('Losses', 490, 90)
  makeCtxFirstColumn();
  ctx.fillText('345', 572, 90)

  makeCtxSecondColumn();
  ctx.fillText('Draws', 490, 125)
  makeCtxFirstColumn();
  ctx.fillText('345', 572, 125)

  makeCtxSecondColumn();
  ctx.fillText('Total', 490, 160)
  makeCtxFirstColumn();
  ctx.fillText('345343', 572, 160)

  makeCtxSecondColumn();
  ctx.fillText('Win Rate', 490, 210)
  makeCtxFirstColumn();
  ctx.fillText('58%', 597, 210)

  makeCtxSecondColumn();
  ctx.fillText('Points', 230, 245)
  makeCtxFirstColumn();
  ctx.fillText('332.32', 326, 245)
  ctx.fillStyle = colorsRanks['Bot'];
  ctx.fillText('Bot', 230, 210)

  makeCtxSecondColumn();
  ctx.fillText('NANAX Points', 490, 245)
  makeCtxFirstColumn();
  ctx.fillText('6', 645, 245)
  ctx.globalAlpha = '0.3';
  ctx.strokeStyle = 'white'
  ctx.stroke();

}).then(() => {
  loadImage('avatar2.jpeg').then((image) => {
    ctx.globalAlpha = '1';
    ctx.drawImage(image, 32, 31, 128, 128);
    ctx.font = '57px "Military Poster"';
    ctx.fillStyle = colorsRanks['Bot'];
    ctx.fillText('macautribes', 60, 175)
    ctx.fillStyle = 'white';
    ctx.font = '43px "Oswald"';
    ctx.fillText('#153', 32, 245)
    const imgBuffer = canvas.toBuffer('image/png')
    fs.writeFileSync('test.png', imgBuffer)
  });
});
