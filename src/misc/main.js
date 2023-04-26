const fs = require('fs');
const { createCanvas, loadImage, registerFont } = require('canvas');
registerFont('src/misc/Oswald-Regular.ttf', { family: 'Oswald Regular' });
registerFont('src/misc/Military Poster.ttf', { family: 'Military Poster Regular' });
const canvas = createCanvas(688, 276);
const ctx = canvas.getContext('2d')
const colorsRanks = {
  'Bot': "#a94700",
  'Lucker': "#00ce21",
  'Strawberry Legend': "#ff8413",
  'Drunken Master': "#13f1ff",
  'Rambo': "#009fd1",
  'Sergeant Mahoney': "#2c5aff",
  'Legend': "#ffd600",
  'Chuck Norris': "#c8ff2c",
  'Pro': "#a513ff",
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



loadImage('src/misc/nanax_logo.png').then((image) => {
  ctx.fillStyle = 'black'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.globalAlpha = '0.2';
  ctx.drawImage(image, 50, 0, image.naturalWidth, image.naturalHeight)
  ctx.globalAlpha = '1';
  ctx.font = '28px Oswald';
  ctx.fillStyle = 'white'



  makeCtxSecondColumn();
  ctx.fillText('Country', 220, 55)
  makeCtxFirstColumn();
  ctx.fillText('Switzerland', 316, 55)

  makeCtxSecondColumn();
  ctx.fillText('Tag', 220, 90)
  makeCtxFirstColumn();
  ctx.fillText('Navi', 316, 90)

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
  ctx.fillText('Points', 220, 210)
  makeCtxFirstColumn();
  ctx.fillText('332.32', 280, 210)
  ctx.fillStyle = colorsRanks['Pro'];
  ctx.fillText('Pro', 220, 245)

  makeCtxSecondColumn();
  ctx.fillText('NANAX Points', 490, 245)
  makeCtxFirstColumn();
  ctx.fillText('6', 645, 245)
  ctx.globalAlpha = '0.3';
  ctx.strokeStyle = 'white'
  ctx.stroke();

}).then(() => {
  loadImage('src/misc/avatar2.jpeg').then((image) => {
    ctx.globalAlpha = '1';
    ctx.shadowOffsetX = -15;
    ctx.shadowOffsetY = -15;
    ctx.shadowColor = colorsRanks['Pro'];
    ctx.shadowBlur = 5;
    ctx.drawImage(image, 45, 31, 128, 128);
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = 0;
    ctx.shadowBlur = 0;
    ctx.font = '57px "Military Poster"';
    ctx.fillStyle = colorsRanks['Pro'];
    ctx.fillText('macautribes', 60, 175)
    ctx.fillStyle = 'white';
    ctx.font = '43px "Oswald"';
    ctx.fillText('#153', 45, 245)
    const imgBuffer = canvas.toBuffer('image/png')
    fs.writeFileSync('src/misc/test.png', imgBuffer)
  });
});
