const fs = require('fs');
const { createCanvas, loadImage, registerFont } = require('canvas');
registerFont('Oswald-Regular.ttf', { family: 'Oswald Regular'});
const canvas = createCanvas(688, 276);
const ctx = canvas.getContext('2d')

loadImage('nanax_logo.png').then((image) => {
  ctx.fillStyle = 'black'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.globalAlpha = '0.2';
  ctx.drawImage(image, 50, 0, image.naturalWidth, image.naturalHeight)
  ctx.globalAlpha = '1';
  ctx.font = '28px Oswald';
  ctx.fillStyle = 'white'



  ctx.fillText('Country', 220, 55)
  ctx.fillText('Switzerland', 316, 55)

  ctx.fillText('Team', 220, 90)
  ctx.fillText('Navi', 316, 90)

  ctx.fillText('Wins', 490, 55)
  ctx.fillText('1345', 572, 55)

  ctx.fillText('Losses', 490, 90)
  ctx.fillText('345', 572, 90)

  ctx.fillText('Draws', 490, 125)
  ctx.fillText('345', 572, 125)

  ctx.fillText('Win Rate', 490, 210)
  ctx.fillText('58%', 597, 210)

  ctx.fillText('Skill', 220, 210)
  ctx.fillText('4389332.32', 280, 210)
  ctx.fillText('"Strawberry Legend"', 220, 245)

  ctx.fillText('NANAX Points', 490, 245)
  ctx.fillText('6', 645, 245)
  ctx.globalAlpha = '0.3';
  ctx.strokeStyle = 'white'
  ctx.stroke();

  const imgBuffer = canvas.toBuffer('image/png')
  fs.writeFileSync('./test.png', imgBuffer)
})
